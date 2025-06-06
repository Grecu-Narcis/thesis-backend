package org.example.postsservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/health")
@CrossOrigin(origins = "*")
public class HealthCheckController {
    @GetMapping("")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
