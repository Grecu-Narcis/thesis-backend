package org.example.followservice.business

import org.example.followservice.exceptions.SameUsernameFollowException
import org.example.followservice.models.Follow
import org.example.followservice.models.FollowKey
import org.example.followservice.repositories.FollowRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.*

@Service
class FollowService(private val followRepository: FollowRepository,
                    private val followNotificationService: FollowNotificationService) {

    @Cacheable(value = ["followersCount"], key = "#username")
    fun countFollowers(username: String): Int {
        return followRepository.countByFollowedUser(username)
    }

    @Cacheable(value = ["followingCount"], key = "#username")
    fun countFollowing(username: String): Int {
        return followRepository.countByFollowingUser(username)
    }

    @Caching(evict = [
        CacheEvict(value = ["followersCount"], key = "#followedUser"),
        CacheEvict(value = ["followingCount"], key = "#followingUser")
    ])
    fun followUser(followingUser: String, followedUser: String) {
        if (followingUser == followedUser)
            throw SameUsernameFollowException("Following user and followed user cannot be the same")

        val follow = Follow(followingUser, followedUser, Date())
        followRepository.save(follow)

        followNotificationService.notifyNewFollow(followingUser, followedUser)
    }

    @Caching(evict = [
        CacheEvict(value = ["followersCount"], key = "#followedUser"),
        CacheEvict(value = ["followingCount"], key = "#followingUser")
    ])
    fun unfollowUser(followingUser: String, followedUser: String) {
        if (followingUser == followedUser)
            throw SameUsernameFollowException("Following user and followed user cannot be the same")

        val follow = followRepository.findById(FollowKey(followingUser, followedUser))

        if (follow.isEmpty) throw Exception("Follow relationship does not exist")

        followRepository.delete(follow.get())
    }

    @Cacheable(value = ["followRelation"], key = "#followingUser-#followedUser")
    fun checkFollow(followingUser: String, followedUser: String): Boolean {
        return followRepository.existsById(FollowKey(followingUser, followedUser))
    }
}