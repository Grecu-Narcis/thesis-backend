package org.example.postsservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.postsservice.business.PostReportService;
import org.example.postsservice.business.PostsNotificationService;
import org.example.postsservice.business.PostsService;
import org.example.postsservice.business.S3Service;
import org.example.postsservice.config.PostPage;
import org.example.postsservice.dto.AddPostDTO;
import org.example.postsservice.dto.LikePostDTO;
import org.example.postsservice.dto.PostsListResponse;
import org.example.postsservice.dto.ReportPostRequest;
import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.example.postsservice.utils.JWTUtils;
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
    private final PostReportService postReportService;
    private final JWTUtils jwtUtils;

    @Autowired
    public PostsController(S3Service s3Service, PostsService postsService,
                           PostsNotificationService postsNotificationService,
                           PostReportService postReportService, JWTUtils jwtUtils) {
        this.s3Service = s3Service;
        this.postsService = postsService;
        this.postsNotificationService = postsNotificationService;
        this.postReportService = postReportService;
        this.jwtUtils = jwtUtils;
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
    public ResponseEntity<?> addPost(@RequestBody AddPostDTO postDTO,
                                     @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Adding post: " + postDTO.getImageKey() + " " + postDTO.getCreatedBy() + " " +
                postDTO.getDescription() + " " + postDTO.getLatitude() + " " + postDTO.getLongitude());

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(postDTO.getCreatedBy()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, @RequestHeader("Authorization") String bearerToken) {
        String authorizedUsername = jwtUtils.getUsernameFromBearerToken(bearerToken);

        try {
            Post requiredPost = this.postsService.getById(postId);

            if (!authorizedUsername.equals(requiredPost.getCreatedBy()))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            this.postsService.deletePost(postId);

            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
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

        PostPage foundPosts = this.postsService.findPostsByFollowedUsers(username, page);

        return ResponseEntity.ok(new PostsListResponse(foundPosts.getContent(), foundPosts.getHasNext()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<PostsListResponse> getPostsByUsername(@PathVariable String username, @RequestParam int page) {
        Logger.log("Getting posts by user: " + username + " page: " + page);

        PostPage foundPosts = this.postsService.findPostsByUsername(username, page);

        return ResponseEntity.ok(new PostsListResponse(foundPosts.getContent(), foundPosts.getHasNext()));
    }

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikePostDTO likePostDTO,
                                      @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Liking post: " + likePostDTO.getPostId() + " username: " + likePostDTO.getUsername());

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(likePostDTO.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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
                                        @PathVariable String username,
                                        @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Unliking post: " + postId + " username: " + username);

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(username))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

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

    @GetMapping("/heatmap")
    public ResponseEntity<?> getPostsForHeatMap(@RequestParam double minLat,
                                                @RequestParam double maxLat,
                                                @RequestParam double minLon,
                                                @RequestParam double maxLon) {
        Logger.log("Getting posts for heatmap: " + minLat + " " + maxLat + " " + minLon + " " + maxLon);

        return ResponseEntity.ok(this.postsService.getPostsForHeatMap(minLat, maxLat, minLon, maxLon));
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportPost(@RequestBody ReportPostRequest reportPostRequest,
                                        @RequestHeader("Authorization") String bearerToken) {

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(reportPostRequest.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            postReportService.createPostReport(reportPostRequest.getPostId(), reportPostRequest.getUsername());

            return ResponseEntity.ok("Reported post");
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
