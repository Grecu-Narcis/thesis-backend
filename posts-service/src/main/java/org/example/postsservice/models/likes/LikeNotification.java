package org.example.postsservice.models.likes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeNotification {
    private final String notificationType = "LIKE";
    private Long postId;
    private String likedBy;
    private String destinationToken;
}
