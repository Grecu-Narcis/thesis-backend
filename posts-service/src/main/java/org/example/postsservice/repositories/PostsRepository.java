package org.example.postsservice.repositories;

import org.example.postsservice.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<Post, Long> {
    @Query(value = """ 
            SELECT * FROM posts, ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) AS distance
            WHERE ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) <= :radius
            ORDER BY distance
            """,
            countQuery = """
            SELECT COUNT(*) FROM posts
            WHERE ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) <= :radius
            """,
            nativeQuery = true)
    Page<Post> findPostsWithinRadius(@Param("point") String point, @Param("radius") double radius, Pageable pageable);

    @Query(value = """ 
            SELECT * FROM posts
            ORDER BY createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM posts
            """,
            nativeQuery = true)
    Page<Post> findPostsByFollowedUsers(@Param("username") String username, Pageable pageable);

    @Query(value = """ 
            SELECT COUNT(*) FROM posts
            """,
            nativeQuery = true)
    int countPostsByFollowedUsers(@Param("username") String username);
}