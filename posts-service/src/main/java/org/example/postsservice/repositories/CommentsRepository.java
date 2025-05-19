package org.example.postsservice.repositories;

import org.example.postsservice.dto.CommentResponseDTO;
import org.example.postsservice.models.comments.Comment;
import org.example.postsservice.models.comments.CommentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, CommentId> {
    @Query(value = """
       SELECT c.createdBy, c.content, c.createdAt, u.profile_image AS profilePicture
       FROM comments c
       INNER JOIN users u on c.createdBy = u.username
       WHERE c.postId = :postId
       ORDER BY createdAt DESC, u.username
""",

    countQuery = """
    SELECT count(*)
       FROM comments c
       INNER JOIN users u on c.createdBy = u.username
       WHERE c.postId = :postId
""",
    nativeQuery = true)
    Page<CommentResponseDTO> findByPostId(Long postId, Pageable pageable);
}
