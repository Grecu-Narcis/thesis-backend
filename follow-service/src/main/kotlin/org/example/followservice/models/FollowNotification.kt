package org.example.followservice.models

data class FollowNotification (
    val notificationType: String = "NEW_FOLLOW",
    val followingUser: String = "",
    val followedUser: String = "",
    val destinationToken: String = ""
)