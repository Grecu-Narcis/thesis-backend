package org.example.postsservice.business;

import org.example.postsservice.models.PostCreatedNotification;
import org.example.postsservice.models.likes.LikeNotification;
import org.example.postsservice.repositories.PostsRepository;
import org.example.postsservice.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.function.Consumer;

@Service
public class PostsNotificationService {
    private final PostsRepository postsRepository;
    private final SqsService sqsService;
    private final RestTemplate restTemplate;
    static int maximumDistanceInMeters = 500;
    static int pageSize = 30;

    @Value("${app.users.service.url}")
    private String usersServiceUrl;

    @Autowired
    public PostsNotificationService(PostsRepository postsRepository, SqsService sqsService, RestTemplate restTemplate) {
        this.postsRepository = postsRepository;
        this.sqsService = sqsService;
        this.restTemplate = restTemplate;
    }

    @Async
    public void notifyNewLike(Long postId, String likedBy, String postCreator) {
        try {
            String token = getNotificationToken(postCreator);

            LikeNotification notification = new LikeNotification(postId, likedBy, token);
            this.sqsService.sendLikeNotification(notification);
        } catch (Exception e) {
            Logger.logError("Failed to get notification token for user: " + postCreator);
        }
    }

    public String getNotificationToken(String username) throws Exception {
        String url = this.usersServiceUrl + "/api/auth/notification-token/" + username;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new Exception("Failed to get notification token for user: " + username);
        }

    }

    @Async
    public void notifyPostAdded(Long postId, String createdBy, double latitude, double longitude, String carBrand, String carModel, int productionYear) {
        String pointWKT = String.format(Locale.US, "POINT(%f %f)", latitude, longitude);

        paginateNearbyUserTokens(pointWKT, createdBy, PostsNotificationService.maximumDistanceInMeters, PostsNotificationService.pageSize, tokensPage -> processPageOfTokens(postId, createdBy, carBrand, carModel, productionYear, tokensPage));
    }

    private void paginateNearbyUserTokens(String pointWKT, String createdBy, int maxDistanceInMeters, int pageSize, Consumer<Page<String>> pageProcessor) {
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<String> tokensPage;

        do {
            tokensPage = postsRepository.findNearbyUsersNotificationTokens(pointWKT, createdBy, maxDistanceInMeters, pageable);

            pageProcessor.accept(tokensPage);
            pageable = tokensPage.nextPageable();
        } while (tokensPage.hasNext());
    }

    public void processPageOfTokens(Long postId, String createdBy, String carBrand, String carModel, int productionYear, Page<String> usersTokensPage) {
        for (String token : usersTokensPage.getContent()) {
            PostCreatedNotification notification = new PostCreatedNotification(postId, createdBy, carBrand, carModel, productionYear, token);

            this.sqsService.sendPostCreatedNotification(notification);
        }
    }
}
