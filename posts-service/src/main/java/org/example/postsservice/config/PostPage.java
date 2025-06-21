package org.example.postsservice.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.postsservice.models.Post;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostPage {
    private List<Post> content;
    private boolean hasNext;

    public boolean getHasNext() {
        return this.hasNext;
    }
}

