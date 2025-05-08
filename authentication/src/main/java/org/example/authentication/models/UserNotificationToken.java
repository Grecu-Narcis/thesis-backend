package org.example.authentication.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users_notification_token")
public class UserNotificationToken {
    @Id
    @Column(name = "username", updatable = false)
    private String username;

    @Column(name = "token", nullable = false)
    private String token;
}