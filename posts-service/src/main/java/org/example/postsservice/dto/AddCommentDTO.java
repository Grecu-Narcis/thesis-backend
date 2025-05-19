package org.example.postsservice.dto;

import lombok.Data;

@Data
public class AddCommentDTO {
    private String createdBy;
    private Long postId;
    private String content;
}
