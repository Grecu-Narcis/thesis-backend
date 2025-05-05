package org.example.postsservice.business;

import org.example.postsservice.models.PostCreatedNotification;
import org.example.postsservice.repositories.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.function.Consumer;

@Service
public class PostsNotificationService {
    private final PostsRepository postsRepository;
    private final SqsService sqsService;
    static int maximumDistanceInMeters = 500;
    static int pageSize = 30;

    @Autowired
    public PostsNotificationService(PostsRepository postsRepository, SqsService sqsService) {
        this.postsRepository = postsRepository;
        this.sqsService = sqsService;
    }

    @Async
    public void notifyPostAdded(Long postId, String createdBy, double latitude, double longitude,
                                String carBrand, String carModel, int productionYear) {
        String pointWKT = String.format(Locale.US, "POINT(%f %f)", latitude, longitude);

        paginateNearbyUserTokens(
                pointWKT,
                createdBy,
                PostsNotificationService.maximumDistanceInMeters,
                PostsNotificationService.pageSize,
                tokensPage -> processPageOfTokens(postId, createdBy, carBrand, carModel, productionYear, tokensPage)
        );
    }

    private void paginateNearbyUserTokens(
            String pointWKT,
            String createdBy,
            int maxDistanceInMeters,
            int pageSize,
            Consumer<Page<String>> pageProcessor) {

        Pageable pageable = PageRequest.of(0, pageSize);
        Page<String> tokensPage;

        do {
            tokensPage = postsRepository.findNearbyUsersNotificationTokens(
                    pointWKT, createdBy, maxDistanceInMeters, pageable
            );

            pageProcessor.accept(tokensPage);
            pageable = tokensPage.nextPageable();
        } while (tokensPage.hasNext());
    }

    public void processPageOfTokens(Long postId, String createdBy, String carBrand, String carModel,
            int productionYear, Page<String> usersTokensPage)
    {
        for (String token : usersTokensPage.getContent()) {
            PostCreatedNotification notification = new PostCreatedNotification(
                    postId,
                    createdBy,
                    carBrand,
                    carModel,
                    productionYear,
                    token
            );

            this.sqsService.sendPostCreatedNotification(notification);
        }
    }
}
