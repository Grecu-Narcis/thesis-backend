package org.example.postsservice.dto;

import lombok.Data;

@Data
public class ReportPostRequest {
    public Long postId;
    public String username;
}
