package org.example.postsservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.postsservice.business.PostsService;
import org.example.postsservice.business.S3Service;
import org.example.postsservice.dto.AddPostDTO;
import org.example.postsservice.dto.LikePostDTO;
import org.example.postsservice.dto.PostsListResponse;
import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostsController {
    private final S3Service s3Service;
    private final PostsService postsService;

    @Autowired
    public PostsController(S3Service s3Service, PostsService postsService) {
        this.s3Service = s3Service;
        this.postsService = postsService;
    }

    @GetMapping(path = "/presignedUrl")
    public ResponseEntity<?> generatePresignedUrl(@RequestParam String type, @RequestParam String key) {
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
        try {
            return ResponseEntity.ok(this.postsService.getById(postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found!");
        }
    }

    @PostMapping(path = "")
    public ResponseEntity<String> addPost(@RequestBody AddPostDTO postDTO) {
        try {
            System.out.println(postDTO);

            this.postsService.addPost(postDTO.getImageKey(), postDTO.getCreatedBy(),
                    postDTO.getDescription(), postDTO.getLatitude(), postDTO.getLongitude(),
                    postDTO.getCarBrand(), postDTO.getCarModel(), postDTO.getProductionYear());

            return ResponseEntity.ok("Post added");
        } catch (AddPostException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPosts(@RequestParam double latitude,
                                            @RequestParam double longitude,
                                            @RequestParam String username,
                                            @RequestParam(name="page") int pageNumber) {
        System.out.println("Nearby posts request: " + latitude + " " + longitude + " " + username + " " + pageNumber);

        List<Post> foundPosts = this.postsService.findNearbyPosts(username, latitude, longitude, pageNumber);
        boolean hasMore = this.postsService.hasMoreNearbyPosts(username, pageNumber);

        return ResponseEntity.ok(new PostsListResponse(foundPosts, hasMore));
    }

    @GetMapping("/followed")
    public ResponseEntity<PostsListResponse> getPostsByFollowedUsers(@RequestParam String username, @RequestParam int page) {
        List<Post> foundPosts = this.postsService.findPostsByFollowedUsers(username, page);
        boolean hasMore = this.postsService.hasMorePostsByFollowedUsers(username, page);

        return ResponseEntity.ok(new PostsListResponse(foundPosts, hasMore));
    }

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikePostDTO likePostDTO) {
        try {
            this.postsService.likePost(likePostDTO.getPostId(), likePostDTO.getUsername());
            System.out.println("Liked post: " + likePostDTO.getPostId() + " username: " + likePostDTO.getUsername());
            return ResponseEntity.ok("Liked post");
        } catch (AlreadyLikedPostException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/like/{postId}/{username}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId,
                                        @PathVariable String username) {
        try {
            System.out.println("Unliked post: " + postId + " username: " + username);
            this.postsService.unlikePost(postId, username);
            return ResponseEntity.noContent().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/liked/{postId}/{username}")
    public ResponseEntity<?> isPostLiked(@PathVariable Long postId,
                                         @PathVariable String username) {
        return ResponseEntity.ok(this.postsService.isLikedByUser(postId, username));
    }
}
