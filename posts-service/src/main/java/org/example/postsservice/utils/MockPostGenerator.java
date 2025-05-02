package org.example.postsservice.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import org.example.postsservice.dto.BrandModelPair;
import org.example.postsservice.models.Post;
import org.example.postsservice.repositories.PostsRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.*;

@Component
public class MockPostGenerator {
    private final PostsRepository postRepository;
    private final RestTemplate restTemplate;

    private final Faker faker = new Faker();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final Random random = new Random();

    private final int MOCK_POST_COUNT = 10000;

    public MockPostGenerator(PostsRepository postRepository, RestTemplate restTemplate) {
        this.postRepository = postRepository;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void generateMockPosts() {
        if (postRepository.count() > MOCK_POST_COUNT) return;

        List<String> usernames = fetchUsernamesFromUserService();
        List<BrandModelPair> carData = loadCarBrandModels();

        System.out.println(usernames);
        System.out.println(carData);

        if (usernames.isEmpty() || carData.isEmpty()) return;

        List<Post> posts = new ArrayList<>();

        for (int i = 0; i < MOCK_POST_COUNT; i++) {
            String username = usernames.get(random.nextInt(usernames.size()));
            BrandModelPair pair = carData.get(random.nextInt(carData.size()));
            String brand = pair.getBrand();
            String model = pair.getModels().get(random.nextInt(pair.getModels().size()));

            Post post = new Post();
            post.setImageKey("mustang.jpg");
            post.setCreatedBy(username);
            post.setDescription(faker.lorem().sentence(10));
            post.setLikesCount(random.nextInt(500));
            post.setCreatedAt(new Date());
            post.setCarBrand(brand);
            post.setCarModel(model);
            post.setProductionYear(1990 + random.nextInt(34));

            double lat = 48 + random.nextDouble();
            double lng = 13 + random.nextDouble();
            Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
            point.setSRID(4326);
            post.setLocation(point);

            posts.add(post);
        }

        postRepository.saveAll(posts);
        System.out.println("✅ Generated " + MOCK_POST_COUNT + " mock posts.");
    }

    private List<String> fetchUsernamesFromUserService() {
        try {
            String url = "http://localhost:8080/api/auth/usernames"; // Replace with real endpoint
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<BrandModelPair> loadCarBrandModels() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("clear_data.json");
            return mapper.readValue(inputStream, new TypeReference<List<BrandModelPair>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

