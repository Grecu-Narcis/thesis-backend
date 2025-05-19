package org.example.authentication.business;

import org.example.authentication.exceptions.TokenNotFoundException;
import org.example.authentication.models.UserNotificationToken;
import org.example.authentication.repositories.UsersNotificationTokenRepository;
import org.example.authentication.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UsersNotificationTokenService {
    private final UsersNotificationTokenRepository usersNotificationTokenRepository;
    private final DynamoDbClient dynamoDbClient;

    @Value("${app.dynamodb.notification.token.table}")
    private String tableName;

    @Autowired
    public UsersNotificationTokenService(UsersNotificationTokenRepository usersNotificationTokenRepository, DynamoDbClient dynamoDbClient) {
        this.usersNotificationTokenRepository = usersNotificationTokenRepository;
        this.dynamoDbClient = dynamoDbClient;
    }

    public void saveUserNotificationToken(UserNotificationToken userNotificationToken) {
        usersNotificationTokenRepository.save(userNotificationToken);
        this.saveUserNotificationTokenToDynamoDb(userNotificationToken);
    }

    public void saveUserNotificationTokenToDynamoDb(UserNotificationToken userNotificationToken) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("username", AttributeValue.builder().s(userNotificationToken.getUsername()).build());
            item.put("token", AttributeValue.builder().s(userNotificationToken.getToken()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder().tableName(this.tableName).item(item).build();

            dynamoDbClient.putItem(putItemRequest);
        }
        catch (Exception e) {
            Logger.logError("Error saving user notification token to DynamoDB: " + e.getMessage());
        }
    }

    public String getUserNotificationToken(String username) throws TokenNotFoundException {
        Optional<UserNotificationToken> tokenOptional = usersNotificationTokenRepository.findById(username);

        if (tokenOptional.isEmpty()) throw new TokenNotFoundException("Token not found!");

        return tokenOptional.get().getToken();
    }
}
