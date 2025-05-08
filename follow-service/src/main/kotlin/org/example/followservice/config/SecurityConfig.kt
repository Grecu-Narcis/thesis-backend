package org.example.followservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().permitAll()
            } // Allow all requests without authentication

            .csrf { csrf -> csrf.disable() }
            .formLogin { login -> login.disable() } // Disable default login form
            .httpBasic { basic -> basic.disable() }// Disable Basic Auth

        return http.build()
    }
}