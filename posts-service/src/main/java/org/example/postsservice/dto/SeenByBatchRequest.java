package org.example.postsservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SeenByBatchRequest {
    private String username;
    private List<Long> postIds;
}
