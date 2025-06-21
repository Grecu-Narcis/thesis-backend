package org.example.postsservice.models.seen_by;

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
@Table(name = "seen_by")
@IdClass(SeenById.class)
public class SeenBy {
    @Id
    @Column(name = "username")
    private String username;

    @Id
    @Column(name = "postId")
    private Long postId;
}
