package org.example.postsservice.models.likes;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LikeId implements Serializable {
    private String username;
    private Long postId;
}
