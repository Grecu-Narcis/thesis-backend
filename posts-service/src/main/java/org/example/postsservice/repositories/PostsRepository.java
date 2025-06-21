package org.example.postsservice.repositories;

import org.example.postsservice.dto.HeatMapPostDTO;
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
    int countByCreatedBy(String createdBy);

    /**
     * Finds posts created by other users, ordered by proximity,
     * filtering out any posts that have 50 or more reports.
     */
    @Query(value = """
        SELECT p.* FROM posts p
        LEFT JOIN post_reports pr ON p.postId = pr.postId
        WHERE p.createdBy <> :username
          AND NOT EXISTS (
              SELECT 1 FROM seen_by sb
              WHERE sb.username = :username AND sb.postId = p.postId
          )
        GROUP BY p.postId
        HAVING COUNT(pr.postId) < 50
        ORDER BY ST_Distance_Sphere(p.location, ST_GeomFromText(:point, 4326))
        """, countQuery = """
        SELECT COUNT(*) FROM (
            SELECT 1 FROM posts p
            LEFT JOIN post_reports pr ON p.postId = pr.postId
            WHERE p.createdBy <> :username
              AND NOT EXISTS (
                  SELECT 1 FROM seen_by sb
                  WHERE sb.username = :username AND sb.postId = p.postId
              )
            GROUP BY p.postId
            HAVING COUNT(pr.postId) < 50
        ) as posts_with_report_count
        """, nativeQuery = true)
    Page<Post> findPostsNearbyUser(@Param("point") String point, @Param("username") String username, Pageable pageable);

    /**
     * Finds posts from users that the specified user follows, ordered by creation date,
     * filtering out any posts that have 50 or more reports.
     */
    @Query(value = """
        SELECT p.* FROM posts p
        INNER JOIN follows f ON p.createdBy = f.followed_user
        LEFT JOIN post_reports pr ON p.postId = pr.postId
        WHERE f.following_user = :username
          AND NOT EXISTS (
              SELECT 1 FROM seen_by sb
              WHERE sb.username = :username AND sb.postId = p.postId
          )
        GROUP BY p.postId
        HAVING COUNT(pr.postId) < 50
        ORDER BY p.createdAt DESC, p.postId DESC
        """, countQuery = """
        SELECT COUNT(*) FROM (
            SELECT 1 FROM posts p
            INNER JOIN follows f ON p.createdBy = f.followed_user
            LEFT JOIN post_reports pr ON p.postId = pr.postId
            WHERE f.following_user = :username
              AND NOT EXISTS (
                  SELECT 1 FROM seen_by sb
                  WHERE sb.username = :username AND sb.postId = p.postId
              )
            GROUP BY p.postId
            HAVING COUNT(pr.postId) < 50
        ) as posts_with_report_count
        """, nativeQuery = true)
    Page<Post> findPostsByFollowedUsers(@Param("username") String username, Pageable pageable);

    @Query(value = """
            SELECT token
            FROM users_notification_token
            INNER JOIN user_location on user_location.username = users_notification_token.username
            WHERE ST_Distance_Sphere(user_location.location, ST_GeomFromText(:point, 4326)) <= :distance AND user_location.username <> :username
            """, nativeQuery = true)
    Page<String> findNearbyUsersNotificationTokens(@Param("point") String point, @Param("username") String username, @Param("distance") int distance, Pageable pageable);

    Page<Post> findPostsByCreatedBy(String username, Pageable pageable);

    @Query(value = """
      SELECT
        postId,
        ST_X(location) AS latitude,
        ST_Y(location) AS longitude
      FROM posts
      WHERE
        ST_X(location) BETWEEN :minLat AND :maxLat
        AND ST_Y(location) BETWEEN :minLon AND :maxLon
  """,
            nativeQuery = true)
    List<HeatMapPostDTO> findPostsForHeatMap(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );
}