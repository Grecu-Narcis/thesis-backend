package org.example.postsservice.business;

import org.example.postsservice.dto.CommentResponseDTO;
import org.example.postsservice.models.comments.Comment;
import org.example.postsservice.repositories.CommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    static int pageSize = 30;

    @Autowired
    public CommentsService(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    public Page<CommentResponseDTO> getCommentsForPost(Long postId, int page) {
        Pageable pageable = PageRequest.of(page, pageSize);

        return this.commentsRepository.findByPostId(postId, pageable);
    }

    public void addComment(String createdBy, Long postId, String content) {
        Comment comment = new Comment(createdBy, postId, content);
        this.commentsRepository.save(comment);
    }
}
