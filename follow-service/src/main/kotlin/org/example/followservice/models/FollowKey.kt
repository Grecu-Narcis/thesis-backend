package org.example.followservice.models

import java.io.Serializable


data class FollowKey(
    val followingUser: String = "",
    val followedUser: String = ""
) : Serializable