package org.example.postsservice.models.likes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
public class Like {

    @Id
    @Column(name = "username")
    private String username;

    @Id
    @Column(name = "postId")
    private Long postId;
}

