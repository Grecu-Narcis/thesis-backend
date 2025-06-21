package org.example.postsservice.controllers;

import org.example.postsservice.business.SeenByService;
import org.example.postsservice.dto.SeenByBatchRequest;
import org.example.postsservice.utils.JWTUtils;
import org.example.postsservice.utils.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class SeenByController {

    private final SeenByService seenByService;
    private final JWTUtils jwtUtils;

    public SeenByController(SeenByService seenByService, JWTUtils jwtUtils) {
        this.seenByService = seenByService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/seen")
    public ResponseEntity<String> markPostsAsSeen(@RequestBody SeenByBatchRequest request,
                                                  @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Marking posts as seen for: " + request.getUsername());

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(request.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        seenByService.markPostsAsSeen(request.getUsername(), request.getPostIds());
        return ResponseEntity.ok("Marked as seen");
    }
}
