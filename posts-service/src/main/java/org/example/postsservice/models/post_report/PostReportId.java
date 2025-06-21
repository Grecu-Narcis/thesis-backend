package org.example.postsservice.models.post_report;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PostReportId implements Serializable {
    private Long postId;
    private String username;
}
