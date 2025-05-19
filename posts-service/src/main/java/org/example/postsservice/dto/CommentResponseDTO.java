package org.example.postsservice.dto;

import java.util.Date;

public interface CommentResponseDTO {
    String getCreatedBy();
    Date getCreatedAt();
    String getContent();
    String getProfilePicture();
}
