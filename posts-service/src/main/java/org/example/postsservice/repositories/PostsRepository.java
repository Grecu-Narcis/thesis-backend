package org.example.postsservice.repositories;

import org.example.postsservice.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, Long> {
    @Query(value = """ 
            SELECT * FROM posts
            WHERE createdBy <> :username
            ORDER BY ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326))
            """,
            countQuery = """
            SELECT COUNT(*) FROM posts
            WHERE createdBy <> :username
            """,
            nativeQuery = true)
    Page<Post> findPostsNearbyUser(@Param("point") String point, @Param("username") String username, Pageable pageable);

    @Query(value = """ 
            SELECT COUNT(*) FROM posts
            WHERE createdBy <> :username
            """,
            nativeQuery = true)
    int countNearbyPosts(@Param("username") String username);

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
            WHERE createdBy <> :username
            """,
            nativeQuery = true)
    int countPostsByFollowedUsers(@Param("username") String username);

    // TODO: Add condition to check if the user is not the one who created the post
    @Query(value = """
    SELECT token
    FROM users_notification_token
    INNER JOIN user_location on user_location.username = users_notification_token.username
    WHERE ST_Distance_Sphere(user_location.location, ST_GeomFromText(:point, 4326)) <= :distance AND 
          user_location.username <> :username
    """,
""",
    nativeQuery = true)
    Page<String> findNearbyUsersNotificationTokens(
            @Param("point") String point,
            @Param("username") String username,
            @Param("distance") int distance,
            Pageable pageable
    );
}