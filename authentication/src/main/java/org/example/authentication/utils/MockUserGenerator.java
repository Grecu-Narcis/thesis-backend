package org.example.authentication.utils;

import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import org.example.authentication.models.User;
import org.example.authentication.repositories.UsersRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
public class MockUserGenerator {
    private final UsersRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Faker faker = new Faker(new Locale("en-US"));

    private final int MOCK_USER_COUNT = 100;

    public MockUserGenerator(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void generateMockUsers() {
        if (userRepository.count() > MOCK_USER_COUNT) return;

        List<User> mockUsers = new ArrayList<>();

        for (int i = 0; i < MOCK_USER_COUNT; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String fullName = firstName + " " + lastName;
            String username = (firstName + "." + lastName + i).toLowerCase();
            String email = username + "@example.com";

            User user = new User();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password"));
            user.setCreatedAt(new Date());

            mockUsers.add(user);
        }

        userRepository.saveAll(mockUsers);
        System.out.println("✅ Generated " + MOCK_USER_COUNT + " mock users.");
    }
}
