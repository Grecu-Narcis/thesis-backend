package org.example.postsservice.repositories;

import org.example.postsservice.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT * FROM posts WHERE ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) <= :radius",
            nativeQuery = true)
    List<Post> findPostsWithinRadius(@Param("point") String point, @Param("radius") double radius);
}