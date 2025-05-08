package org.example.followservice.controllers

import org.example.followservice.business.FollowService
import org.example.followservice.dto.FollowUserRequest
import org.example.followservice.exceptions.SameUsernameFollowException
import org.example.followservice.utils.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follow")
@CrossOrigin(origins = ["*"])
class FollowController(private val followService: FollowService) {

    @GetMapping("/followers-count/{username}")
    fun getFollowersCount(@PathVariable username: String): ResponseEntity<Any> {
        Logger.log("Getting followers count for user: $username")

        try {
            val count = followService.countFollowers(username)
            return ResponseEntity.ok(mapOf("count" to count))
        }
        catch (e: Exception) {
            Logger.logError("Error getting followers count: ${e.message}")
            return ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }

    @GetMapping("/following-count/{username}")
    fun getFollowingCount(@PathVariable username: String): ResponseEntity<Any> {
        Logger.log("Getting following count for user: $username")

        try {
            val count = followService.countFollowing(username)
            return ResponseEntity.ok(mapOf("count" to count))
        }
        catch (e: Exception) {
            Logger.logError("Error getting following count: ${e.message}")
            return ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }

    @PostMapping("")
    fun followUser(@RequestBody followUserRequest: FollowUserRequest): ResponseEntity<Any> {
        Logger.log("Following user: ${followUserRequest.followingUser}, followed user: ${followUserRequest.followedUser}")

        try {
            followService.followUser(followUserRequest.followingUser, followUserRequest.followedUser)
            return ResponseEntity.ok(mapOf("message" to "Followed successfully"))
        }

        catch (e: SameUsernameFollowException) {
            Logger.logError("Error following user: ${e.message}")

            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }

        catch (e: Exception) {
            Logger.logError("Error following user: ${e.message}")
            return ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }

    @DeleteMapping("/{followingUser}/{followedUser}")
    fun unfollowUser(@PathVariable followingUser: String, @PathVariable followedUser: String): ResponseEntity<Any> {
        Logger.log("Unfollowing user: $followingUser, followed user: $followedUser")

        try {
            followService.unfollowUser(followingUser, followedUser)
            return ResponseEntity.ok(mapOf("message" to "Unfollowed successfully"))
        }
        catch (e: SameUsernameFollowException) {
            Logger.logError("Error unfollowing user: ${e.message}")

            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
        catch (e: Exception) {
            Logger.logError("Error unfollowing user: ${e.message}")
            return ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }

    @GetMapping("/follows")
    fun checkFollowStatus(@RequestParam followingUser: String, @RequestParam followedUser: String): ResponseEntity<Any> {
        Logger.log("Checking follow status for user: $followingUser, followed user: $followedUser")

        try {
            val isFollowing = followService.checkFollow(followingUser, followedUser)
            return ResponseEntity.ok(mapOf("isFollowing" to isFollowing))
        }
        catch (e: Exception) {
            Logger.logError("Error checking follow status: ${e.message}")
            return ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }
}