package org.example.followservice.business

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.followservice.models.FollowNotification
import org.example.followservice.utils.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse

@Service
class SqsService @Autowired constructor(private val sqsClient: SqsClient) {
    private val objectMapper = ObjectMapper()

    @Value("\${aws.sqs.queue.url}")
    lateinit var queueUrl: String

    fun sendMessage(messageBody: String) {
        val request: SendMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(messageBody)
            .build()

        val response: SendMessageResponse = sqsClient.sendMessage(request)
        Logger.log("Message ID: " + response.messageId())
    }

    fun sendNewFollowNotification(notification: FollowNotification) {
        Logger.log("✅ Sending new follow notification: $notification")
        try {
            val messageBody = objectMapper.writeValueAsString(notification)
            sendMessage(messageBody)
        } catch (e: Exception) {
            System.err.println("🛑 Failed to send notification: " + e.message)
        }
    }
}