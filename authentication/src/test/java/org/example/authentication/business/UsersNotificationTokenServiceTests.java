package org.example.authentication.business;

import org.example.authentication.exceptions.TokenNotFoundException;
import org.example.authentication.models.UserNotificationToken;
import org.example.authentication.repositories.UsersNotificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class UsersNotificationTokenServiceTests {

    @Mock
    private UsersNotificationTokenRepository tokenRepository;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private UsersNotificationTokenService service;

    @BeforeEach
    void init() throws Exception {
        // Set the private field manually (since @Value won't be injected here)
        var field = UsersNotificationTokenService.class.getDeclaredField("tableName");
        field.setAccessible(true);
        field.set(service, "NotificationTokensTable");
    }

    @Test
    void testSaveUserNotificationToken_callsRepoAndDynamoDb() {
        UserNotificationToken token = new UserNotificationToken("john_doe", "token123");

        service.saveUserNotificationToken(token);

        verify(tokenRepository).save(token);
        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void testGetUserNotificationToken_success() throws TokenNotFoundException {
        when(tokenRepository.findById("alice"))
                .thenReturn(Optional.of(new UserNotificationToken("alice", "abc123")));

        String token = service.getUserNotificationToken("alice");

        assertEquals("abc123", token);
    }

    @Test
    void testGetUserNotificationToken_notFound() {
        when(tokenRepository.findById("bob")).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class, () -> service.getUserNotificationToken("bob"));
    }

    @Test
    void testSaveUserNotificationTokenToDynamoDb_correctRequest() {
        UserNotificationToken token = new UserNotificationToken("jane", "notiftoken987");

        service.saveUserNotificationTokenToDynamoDb(token);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());

        PutItemRequest request = captor.getValue();
        assertEquals("NotificationTokensTable", request.tableName());
        assertEquals("jane", request.item().get("username").s());
        assertEquals("notiftoken987", request.item().get("token").s());
    }
}
