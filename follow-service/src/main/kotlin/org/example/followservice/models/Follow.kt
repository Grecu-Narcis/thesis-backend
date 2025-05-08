package org.example.followservice.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import java.util.Date


@Entity(name = "follows")
@IdClass(FollowKey::class)
data class Follow(
    @Id
    @Column(name = "following_user")
    val followingUser: String = "",

    @Id
    @Column(name = "followed_user")
    val followedUser: String = "",
    val followedAt: Date = Date(),
)