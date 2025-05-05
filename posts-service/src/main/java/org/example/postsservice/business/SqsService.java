package org.example.postsservice.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.postsservice.models.PostCreatedNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
public class SqsService {
    private final SqsClient sqsClient;
    static String queueUrl = "https://sqs.eu-central-1.amazonaws.com/841162677495/NotificationsQueue"; // your full queue URL
    private final ObjectMapper objectMapper;

    @Autowired
    public SqsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(String messageBody) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(SqsService.queueUrl)
                .messageBody(messageBody)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(request);
        System.out.println("Message ID: " + response.messageId());
    }

    public void sendPostCreatedNotification(PostCreatedNotification notification) {
        try {
            String messageBody = objectMapper.writeValueAsString(notification);
            sendMessage(messageBody);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
