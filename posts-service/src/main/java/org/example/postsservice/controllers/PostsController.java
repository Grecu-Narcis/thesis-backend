package org.example.postsservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.postsservice.business.PostsNotificationService;
import org.example.postsservice.business.PostsService;
import org.example.postsservice.business.S3Service;
import org.example.postsservice.business.SqsService;
import org.example.postsservice.dto.AddPostDTO;
import org.example.postsservice.dto.LikePostDTO;
import org.example.postsservice.dto.PostsListResponse;
import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.example.postsservice.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostsController {
    private final S3Service s3Service;
    private final PostsService postsService;
    private final PostsNotificationService postsNotificationService;
    private final SqsService sqsService;

    @Autowired
    public PostsController(S3Service s3Service, PostsService postsService,
                           PostsNotificationService postsNotificationService, SqsService sqsService) {
        this.s3Service = s3Service;
        this.postsService = postsService;
        this.sqsService = sqsService;
        this.postsNotificationService = postsNotificationService;
    }

    @GetMapping(path = "/presignedUrl")
    public ResponseEntity<?> generatePresignedUrl(@RequestParam String type, @RequestParam String key) {
        Logger.log("Generating presigned URL for key: " + key);

        String generatedUrl = this.s3Service.createPresignedUrl(
                "car-spot-bucket",
                key,
                type
        );

        Map<String, String> response = new HashMap<>();
        response.put("url", generatedUrl);
        response.put("key", key);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        Logger.log("Getting post with ID: " + postId);

        try {
            return ResponseEntity.ok(this.postsService.getById(postId));
        } catch (Exception e) {
            Logger.log("Error getting post with ID: " + postId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found!");
        }
    }

    @PostMapping(path = "")
    public ResponseEntity<?> addPost(@RequestBody AddPostDTO postDTO) {
        Logger.log("Adding post: " + postDTO.getImageKey() + " " + postDTO.getCreatedBy() + " " +
                postDTO.getDescription() + " " + postDTO.getLatitude() + " " + postDTO.getLongitude());

        try {
            Post createdPost = this.postsService.addPost(postDTO.getImageKey(), postDTO.getCreatedBy(),
                    postDTO.getDescription(), postDTO.getLatitude(), postDTO.getLongitude(),
                    postDTO.getCarBrand(), postDTO.getCarModel(), postDTO.getProductionYear());

            this.postsNotificationService.notifyPostAdded(createdPost.getPostId(), postDTO.getCreatedBy(), postDTO.getLatitude(),
                    postDTO.getLongitude(), postDTO.getCarBrand(), postDTO.getCarModel(), postDTO.getProductionYear());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (AddPostException e) {
            Logger.log("Error adding post: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPosts(@RequestParam double latitude,
                                            @RequestParam double longitude,
                                            @RequestParam String username,
                                            @RequestParam(name="page") int pageNumber) {
        Logger.log("Getting nearby posts for user: " + username + " latitude: " + latitude + " longitude: " + longitude);

        Page<Post> foundPosts = this.postsService.findNearbyPosts(username, latitude, longitude, pageNumber);

        return ResponseEntity.ok(new PostsListResponse(foundPosts.getContent(), foundPosts.hasNext()));
    }

    @GetMapping("/followed")
    public ResponseEntity<PostsListResponse> getPostsByFollowedUsers(@RequestParam String username, @RequestParam int page) {
        Logger.log("Getting posts by followed users for user: " + username + " page: " + page);

        Page<Post> foundPosts = this.postsService.findPostsByFollowedUsers(username, page);

        return ResponseEntity.ok(new PostsListResponse(foundPosts.getContent(), foundPosts.hasNext()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<PostsListResponse> getPostsByUsername(@PathVariable String username, @RequestParam int page) {
        Logger.log("Getting posts by user: " + username + " page: " + page);

        Page<Post> foundPosts = this.postsService.findPostsByUsername(username, page);

        return ResponseEntity.ok(new PostsListResponse(foundPosts.getContent(), foundPosts.hasNext()));
    }

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikePostDTO likePostDTO) {
        Logger.log("Liking post: " + likePostDTO.getPostId() + " username: " + likePostDTO.getUsername());

        try {
            this.postsService.likePost(likePostDTO.getPostId(), likePostDTO.getUsername());
            return ResponseEntity.ok("Liked post");
        } catch (AlreadyLikedPostException e) {
            Logger.logError("Post already liked by this user!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (PostNotFoundException e) {
            Logger.logError("Post not found!");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/like/{postId}/{username}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId,
                                        @PathVariable String username) {
        Logger.log("Unliking post: " + postId + " username: " + username);

        try {
            this.postsService.unlikePost(postId, username);
            return ResponseEntity.noContent().build();
        } catch (PostNotFoundException e) {
            Logger.logError("Post not found!");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/liked/{postId}/{username}")
    public ResponseEntity<?> isPostLiked(@PathVariable Long postId,
                                         @PathVariable String username) {
        Logger.log("Checking if post is liked: " + postId + " username: " + username);

        return ResponseEntity.ok(this.postsService.isLikedByUser(postId, username));
    }

    @GetMapping("/count/{username}")
    public ResponseEntity<?> getPostsCount(@PathVariable String username) {
        Logger.log("Count posts request: " + username);

        int count = this.postsService.countPostsByUser(username);

        return ResponseEntity.ok(Map.of("count", count));
    }
}
