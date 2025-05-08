package org.example.followservice.dto

data class FollowUserRequest(
    val followingUser: String,
    val followedUser: String
)