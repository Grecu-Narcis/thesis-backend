package org.example.postsservice.controllers;

import org.example.postsservice.business.CommentsService;
import org.example.postsservice.dto.AddCommentDTO;
import org.example.postsservice.dto.CommentResponseDTO;
import org.example.postsservice.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentsController {
    private final CommentsService commentsService;
    private final JWTUtils jwtUtils;

    @Autowired
    public CommentsController(CommentsService commentsService, JWTUtils jwtUtils) {
        this.commentsService = commentsService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsForPost(@PathVariable Long postId, @RequestParam int page) {
        try {
            Page<CommentResponseDTO> comments = this.commentsService.getCommentsForPost(postId, page);
            Map<String, Object> response = new HashMap<>();

            response.put("comments", comments.toList());
            response.put("hasMore", comments.hasNext());

            return ResponseEntity.ok(response);
        }

        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("")
    public ResponseEntity<?> addComment(@RequestBody AddCommentDTO commentDTO,
                                        @RequestHeader("Authorization") String bearerToken) {
        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(commentDTO.getCreatedBy()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            this.commentsService.addComment(commentDTO.getCreatedBy(), commentDTO.getPostId(), commentDTO.getContent());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
