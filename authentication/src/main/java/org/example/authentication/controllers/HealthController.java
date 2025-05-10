package org.example.authentication.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/health")
@CrossOrigin(origins = "*")
public class HealthController {
    @GetMapping("")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
