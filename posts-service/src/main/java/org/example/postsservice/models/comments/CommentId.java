package org.example.postsservice.models.comments;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommentId implements Serializable {
    private Long postId;
    private String createdBy;
    private Date createdAt;
}
