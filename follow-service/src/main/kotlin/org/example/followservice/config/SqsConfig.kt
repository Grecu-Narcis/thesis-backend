package org.example.followservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient

@Configuration
class SqsConfig {
    @Bean
    fun sqsClient(): SqsClient {
        return SqsClient.builder()
            .region(Region.EU_CENTRAL_1)
            .build()
    }
}