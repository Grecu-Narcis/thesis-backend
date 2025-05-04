package org.example.postsservice.repositories;

import org.example.postsservice.models.likes.Like;
import org.example.postsservice.models.likes.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikesRepository extends JpaRepository<Like, LikeId> {
    boolean existsByUsernameAndPostId(String username, Long postId);
    int countByPostId(Long postId);
    void deleteByUsernameAndPostId(String username, Long postId);
}
