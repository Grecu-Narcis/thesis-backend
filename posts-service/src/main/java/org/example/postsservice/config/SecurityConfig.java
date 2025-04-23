package org.example.postsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests without authentication
                )
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF (optional, useful for APIs)
                .formLogin(AbstractHttpConfigurer::disable) // Disable default login form
                .httpBasic(AbstractHttpConfigurer::disable); // Disable Basic Auth

        return http.build();
    }
}