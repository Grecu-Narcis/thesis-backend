package org.example.postsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.postsservice.models.Post;

import java.util.List;

@Data
@AllArgsConstructor
public class PostsListResponse {
    List<Post> posts;
    boolean hasMore;
}
