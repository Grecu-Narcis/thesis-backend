package org.example.postsservice.business;

import org.example.postsservice.dto.CommentResponseDTO;

import java.util.Date;

public class MockCommentResponseDTO implements CommentResponseDTO {
    private final String createdBy;
    private final Date createdAt;
    private final String content;
    private final String profilePicture;

    public MockCommentResponseDTO(String createdBy, Date createdAt, String content, String profilePicture) {
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.content = content;
        this.profilePicture = profilePicture;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getProfilePicture() {
        return profilePicture;
    }
}
