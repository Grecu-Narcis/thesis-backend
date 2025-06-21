package org.example.postsservice.models.post_report;

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
@Table(name = "post_reports")
@IdClass(PostReportId.class)
public class PostReport {
    @Id
    @Column(name = "postId")
    private Long postId;

    @Id
    @Column(name = "username")
    private String username;
}
