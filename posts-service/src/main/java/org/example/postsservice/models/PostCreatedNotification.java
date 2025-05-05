package org.example.postsservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCreatedNotification {
    private final String notificationType = "POST_CREATED";
    private Long postId;
    private String createdBy;
    private String carBrand;
    private String carModel;
    private int productionYear;
    private String destinationToken;
}
