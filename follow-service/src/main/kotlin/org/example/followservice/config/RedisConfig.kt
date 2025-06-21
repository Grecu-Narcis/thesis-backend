package org.example.followservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig {
    @Value("\${REDIS_HOST}")
    private val redisHost: String? = null

    @Value("\${REDIS_PORT}")
    private val redisPort = 0

    @Value("\${REDIS_PASSWORD}")
    private val redisPassword: String? = null

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration()
        redisConfig.hostName = redisHost!!
        redisConfig.port = redisPort
        redisConfig.password = RedisPassword.of(redisPassword)

        val clientConfig = LettuceClientConfiguration.builder()
            .useSsl()
            .build()

        return LettuceConnectionFactory(redisConfig, clientConfig)
    }
}