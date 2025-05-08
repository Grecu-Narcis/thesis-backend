package org.example.followservice.repositories

import org.example.followservice.models.Follow
import org.example.followservice.models.FollowKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FollowRepository : JpaRepository<Follow, FollowKey> {
    fun countByFollowedUser(followedUser: String): Int
    fun countByFollowingUser(followingUser: String): Int
}