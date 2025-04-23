package org.example.postsservice.business;

import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.models.Post;
import org.example.postsservice.repositories.PostsRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final GeometryFactory geometryFactory;
    private final int SRID = 4326;


    @Autowired
    public PostsService(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
        this.geometryFactory = new GeometryFactory();
    }

    // TODO: Add post validator
    public void addPost(String imageKey, String createdBy, String description, double latitude, double longitude,
                        String carBrand, String carModel, int productionYear) throws AddPostException {
        try {
            Point postLocation = this.geometryFactory.createPoint(new Coordinate(longitude, latitude));
            postLocation.setSRID(this.SRID);

            Post postToAdd = new Post(imageKey, createdBy, description, postLocation, carBrand, carModel, productionYear);

            this.postsRepository.save(postToAdd);
        }
        catch (Exception e) {
            throw new AddPostException("Failed to add post!");
        }
    }

    public Post getById(Long postId) throws Exception {
        Optional<Post> post = this.postsRepository.findById(postId);

        if (post.isEmpty())
            throw new Exception("Post not found!");

        return post.get();
    }

    public List<Post> getNearbyPosts(double latitude, double longitude) {
        String pointWKT = String.format(Locale.US, "POINT(%f %f)", latitude, longitude);
        return this.postsRepository.findPostsWithinRadius(pointWKT, 1000);
    }
}
