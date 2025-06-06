package org.example.followservice.business

import org.example.followservice.models.FollowNotification
import org.example.followservice.utils.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class FollowNotificationService @Autowired constructor(
    private val sqsService: SqsService,
    private val restTemplate: RestTemplate,
) {
    @Value("\${app.users.service.url}")
    lateinit var usersServiceUrl: String

    @Async
    fun notifyNewFollow(followingUser: String, followedUser: String) {
        try {
            val notificationToken = getNotificationToken(followedUser)
            val notification = FollowNotification(
                followingUser = followingUser, followedUser = followedUser, destinationToken = notificationToken
            )

            this.sqsService.sendNewFollowNotification(notification)
        } catch (e: Exception) {
            Logger.logError("Failed to get notification token for user $followedUser: ${e.message}")
            return
        }
        catch (e: RuntimeException) {
            Logger.logError("Failed to send notification for user $followedUser: ${e.message}")
            return
        }
    }

    fun getNotificationToken(username: String): String {
        val url = "$usersServiceUrl/api/auth/notification-token/$username"
        val response = restTemplate.getForEntity(url, String::class.java)
        return if (response.statusCode.is2xxSuccessful) {
            response.body!!
        } else {
            throw Exception("Failed to get notification token for user: $username")
        }
    }
}