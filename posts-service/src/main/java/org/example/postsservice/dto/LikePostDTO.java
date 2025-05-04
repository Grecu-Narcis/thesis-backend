package org.example.postsservice.dto;

import lombok.Data;

@Data
public class LikePostDTO {
    private Long postId;
    private String username;
}
