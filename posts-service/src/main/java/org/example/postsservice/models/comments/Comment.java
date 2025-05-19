package org.example.postsservice.models.comments;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
@IdClass(CommentId.class)
public class Comment {
    @Id
    @Column(name = "postId")
    private Long postId;

    @Id
    @Column(name = "createdBy")
    private String createdBy;

    @Id
    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "content")
    private String content;

    public Comment(String createdBy, Long postId, String content) {
        this.createdBy = createdBy;
        this.postId = postId;
        this.content = content;
        this.createdAt = new Date();
    }
}
