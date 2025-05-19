package org.example.postsservice.utils;

import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import org.example.postsservice.models.comments.Comment;
import org.example.postsservice.repositories.CommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class MockCommentsGenerator {
    private final RestTemplate restTemplate;
    private final Faker faker = new Faker();

    static Long postId = 20071L;
    static int MOCK_COMMENTS_COUNT = 100;
    private final CommentsRepository commentsRepository;
    private final Random random = new Random();

    @Autowired
    public MockCommentsGenerator(RestTemplate restTemplate, CommentsRepository commentsRepository) {
        this.restTemplate = restTemplate;
        this.commentsRepository = commentsRepository;
    }

    @PostConstruct
    public void generateMockPosts() {
        if (commentsRepository.count() >= MOCK_COMMENTS_COUNT) return;

        List<String> usernames = fetchUsernamesFromUserService();

        System.out.println(usernames);

        if (usernames.isEmpty()) return;

        List<Comment> comments = new ArrayList<>();

        for (int i = 0; i < MOCK_COMMENTS_COUNT; i++) {
            String username = usernames.get(random.nextInt(usernames.size()));
            usernames.remove(username);
            String content = faker.howIMetYourMother().catchPhrase();

            Comment comment = new Comment(username, postId, content);

            comments.add(comment);
        }

        commentsRepository.saveAll(comments);
        System.out.println("✅ Generated " + MOCK_COMMENTS_COUNT + " mock comments.");
    }

    private List<String> fetchUsernamesFromUserService() {
        try {
            String url = "http://localhost:8080/api/auth/usernames";
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            Logger.logError(e.getMessage());
            return Collections.emptyList();
        }
    }
}
