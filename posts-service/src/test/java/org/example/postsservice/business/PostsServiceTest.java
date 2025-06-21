package org.example.postsservice.business;

import org.example.postsservice.config.PostPage;
import org.example.postsservice.dto.HeatMapPostDTO;
import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.example.postsservice.models.likes.Like;
import org.example.postsservice.repositories.LikesRepository;
import org.example.postsservice.repositories.PostsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PostsServiceTest {

    @Mock private PostsRepository postsRepository;
    @Mock private LikesRepository likesRepository;
    @Mock private PostsNotificationService postsNotificationService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @InjectMocks private PostsService postsService;

    private Post mockPost;

    @BeforeEach
    void setup() {
        mockPost = new Post("img.jpg", "alice", "a car", null, "BMW", "M3", 2021);
        mockPost.setPostId(1L);
        mockPost.setLikesCount(0);
    }

    @Test
    void addPost_success() throws AddPostException {
        when(redisTemplate.keys(any())).thenReturn(Set.of());
        when(postsRepository.save(any(Post.class))).thenReturn(mockPost);

        Post post = postsService.addPost("img.jpg", "alice", "desc", 45.0, 21.0, "BMW", "M3", 2021);

        assertEquals("alice", post.getCreatedBy());
        assertEquals("BMW", post.getCarBrand());
    }

    @Test
    void getById_shouldReturnPostIfFound() throws Exception {
        when(postsRepository.findById(1L)).thenReturn(Optional.of(mockPost));

        Post result = postsService.getById(1L);

        assertEquals("alice", result.getCreatedBy());
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(postsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> postsService.getById(99L));
    }

    @Test
    void likePost_shouldIncreaseLikesAndNotify() throws Exception {
        when(postsRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(likesRepository.existsByUsernameAndPostId("bob", 1L)).thenReturn(false);

        postsService.likePost(1L, "bob");

        assertEquals(1, mockPost.getLikesCount());
        verify(postsRepository).save(mockPost);
        verify(likesRepository).save(any(Like.class));
        verify(postsNotificationService).notifyNewLike(eq(1L), eq("bob"), eq("alice"));
    }

    @Test
    void likePost_shouldThrowIfAlreadyLiked() {
        when(postsRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(likesRepository.existsByUsernameAndPostId("bob", 1L)).thenReturn(true);

        assertThrows(AlreadyLikedPostException.class, () -> postsService.likePost(1L, "bob"));
    }

    @Test
    void likePost_shouldThrowIfPostNotFound() {
        when(postsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postsService.likePost(1L, "bob"));
    }

    @Test
    void unlikePost_shouldDecreaseLikes() throws PostNotFoundException {
        mockPost.setLikesCount(5);
        when(postsRepository.findById(1L)).thenReturn(Optional.of(mockPost));

        postsService.unlikePost(1L, "bob");

        assertEquals(4, mockPost.getLikesCount());
        verify(likesRepository).deleteByUsernameAndPostId("bob", 1L);
    }

    @Test
    void isLikedByUser_shouldReturnTrueIfExists() {
        when(likesRepository.existsByUsernameAndPostId("alice", 1L)).thenReturn(true);
        assertTrue(postsService.isLikedByUser(1L, "alice"));
    }

    @Test
    void countPostsByUser_shouldReturnCount() {
        when(postsRepository.countByCreatedBy("alice")).thenReturn(7);
        assertEquals(7, postsService.countPostsByUser("alice"));
    }

    @Test
    void findPostsByUsername_shouldReturnPosts() {
        Page<Post> page = new PageImpl<>(List.of(mockPost));
        when(postsRepository.findPostsByCreatedBy(eq("alice"), any(Pageable.class))).thenReturn(page);

        PostPage result = postsService.findPostsByUsername("alice", 0);

        assertEquals(1, result.getContent().size());
        assertEquals("alice", result.getContent().get(0).getCreatedBy());
        assertFalse(result.getHasNext());
    }

    @Test
    void findNearbyPosts_shouldReturnPageOfPosts() {
        Page<Post> page = new PageImpl<>(List.of(mockPost));
        when(postsRepository.findPostsNearbyUser(anyString(), eq("alice"), any(Pageable.class)))
                .thenReturn(page);

        Page<Post> result = postsService.findNearbyPosts("alice", 45.0, 21.0, 0);

        assertEquals(1, result.getTotalElements());
        assertEquals("alice", result.getContent().get(0).getCreatedBy());
    }

    @Test
    void findPostsByFollowedUsers_shouldReturnPageOfPosts() {
        Page<Post> page = new PageImpl<>(List.of(mockPost));
        when(postsRepository.findPostsByFollowedUsers(eq("alice"), any(Pageable.class)))
                .thenReturn(page);

        PostPage result = postsService.findPostsByFollowedUsers("alice", 0);

        assertEquals(1, result.getContent().size());
        assertEquals("alice", result.getContent().get(0).getCreatedBy());
    }

    @Test
    void getPostsForHeatMap_shouldReturnHeatMapPostDTOs() {
        List<HeatMapPostDTO> heatmapData = List.of(
                new MockHeatMapPostDTO(1L, 45.0, 21.0),
                new MockHeatMapPostDTO(2L, 45.1, 21.1)
        );

        when(postsRepository.findPostsForHeatMap(44.0, 46.0, 20.0, 22.0)).thenReturn(heatmapData);

        List<HeatMapPostDTO> result = postsService.getPostsForHeatMap(44.0, 46.0, 20.0, 22.0);

        assertEquals(2, result.size());
        assertEquals(45.0, result.get(0).getLatitude());
        assertEquals(21.1, result.get(1).getLongitude());
    }

}
