package org.example.postsservice.models.seen_by;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SeenById implements Serializable {
    private Long postId;
    private String username;
}
