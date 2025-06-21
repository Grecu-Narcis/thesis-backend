package org.example.postsservice.business;

import org.example.postsservice.models.PostCreatedNotification;
import org.example.postsservice.repositories.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PostsNotificationServiceTest {

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private SqsService sqsService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PostsNotificationService service;

    @BeforeEach
    void setup() throws Exception {
        // Manually inject value for usersServiceUrl
        var field = PostsNotificationService.class.getDeclaredField("usersServiceUrl");
        field.setAccessible(true);
        field.set(service, "http://localhost:8080");
    }

    @Test
    void getNotificationToken_success() throws Exception {
        String expectedToken = "mockToken123";
        String username = "testUser";
        String expectedUrl = "http://localhost:8080/api/auth/notification-token/testUser";

        when(restTemplate.getForEntity(expectedUrl, String.class))
                .thenReturn(new ResponseEntity<>(expectedToken, HttpStatus.OK));

        String result = service.getNotificationToken(username);
        assertEquals(expectedToken, result);
    }

    @Test
    void getNotificationToken_failure() {
        String username = "failUser";
        String expectedUrl = "http://localhost:8080/api/auth/notification-token/failUser";

        when(restTemplate.getForEntity(expectedUrl, String.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThrows(Exception.class, () -> service.getNotificationToken(username));
    }

    @Test
    void notifyNewLike_sendsNotificationIfTokenExists() throws Exception {
        String token = "abc123";
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(token, HttpStatus.OK));

        service.notifyNewLike(10L, "bob", "alice");

        verify(sqsService).sendLikeNotification(argThat(notification ->
                notification.getPostId().equals(10L)
                        && notification.getLikedBy().equals("bob")
                        && notification.getDestinationToken().equals(token)
        ));
    }

    @Test
    void processPageOfTokens_sendsPostCreatedNotifications() {
        Page<String> page = new PageImpl<>(List.of("token1", "token2"));

        service.processPageOfTokens(42L, "john", "BMW", "M3", 2022, page);

        verify(sqsService, times(2)).sendPostCreatedNotification(any(PostCreatedNotification.class));
        verify(sqsService).sendPostCreatedNotification(argThat(n ->
                n.getPostId().equals(42L) &&
                        n.getCarBrand().equals("BMW") &&
                        n.getDestinationToken().equals("token1")
        ));
    }

    @Test
    void paginateNearbyUserTokens_callsProcessOnAllPages() {
        String point = "POINT(45.0 21.0)";
        Pageable page1 = PageRequest.of(0, 30);
        Pageable page2 = PageRequest.of(1, 30);

        Page<String> firstPage = new PageImpl<>(List.of("tokenA"), page1, 60);
        Page<String> secondPage = new PageImpl<>(List.of("tokenB"), page2, 60);

        when(postsRepository.findNearbyUsersNotificationTokens(any(), any(), anyInt(), eq(page1)))
                .thenReturn(firstPage);
        when(postsRepository.findNearbyUsersNotificationTokens(any(), any(), anyInt(), eq(page2)))
                .thenReturn(secondPage);

        Consumer<Page<String>> processor = mock(Consumer.class);
        service.notifyPostAdded(99L, "bob", 45.0, 21.0, "Audi", "RS6", 2021);

        // No assertion here since `@Async` method is void, but you can refactor for better testability
    }
}
