package org.example.followservice.controllers

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/follow/health")
@CrossOrigin(origins = ["*"])
class HealthCheckController {
    @GetMapping("")
    fun healthCheck(): String {
        return "Follow Service is up and running"
    }
}