package org.example.postsservice.business;

import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.example.postsservice.models.likes.Like;
import org.example.postsservice.repositories.LikesRepository;
import org.example.postsservice.repositories.PostsRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final LikesRepository likesRepository;
    private final GeometryFactory geometryFactory;

    private final int SRID = 4326;
    private final int pageSize = 30;

    @Autowired
    public PostsService(PostsRepository postsRepository, LikesRepository likesRepository) {
        this.postsRepository = postsRepository;
        this.likesRepository = likesRepository;
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

    public List<Post> findNearbyPosts(String username, double latitude, double longitude, int pageNumber) {
        String pointWKT = String.format(Locale.US, "POINT(%f %f)", latitude, longitude);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return this.postsRepository.findPostsNearbyUser(pointWKT, username, pageable).toList();
    }

    public List<Post> findPostsByFollowedUsers(String username, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return this.postsRepository.findPostsByFollowedUsers(username, pageable).toList();
    }

    public boolean hasMoreNearbyPosts(String username, int pageNumber) {
        return this.postsRepository.countNearbyPosts(username) > (pageNumber + 1) * pageSize;
    }

    public boolean hasMorePostsByFollowedUsers(String username, int pageNumber) {
        return this.postsRepository.countPostsByFollowedUsers(username) > (pageNumber + 1) * pageSize;
    }

    public void likePost(Long postId, String username) throws AlreadyLikedPostException, PostNotFoundException {
        Optional<Post> requiredPost = this.postsRepository.findById(postId);

        if (requiredPost.isEmpty()) throw new PostNotFoundException("Post not found!");
        if (this.likesRepository.existsByUsernameAndPostId(username, postId))
            throw new AlreadyLikedPostException("Post is already liked by this user!");

        Post post = requiredPost.get();
        post.setLikesCount(post.getLikesCount() + 1);

        this.postsRepository.save(post);
        this.likesRepository.save(new Like(username, postId));
    }

    @Transactional
    public void unlikePost(Long postId, String username) throws PostNotFoundException {
        Optional<Post> requiredPost = this.postsRepository.findById(postId);

        if (requiredPost.isEmpty()) throw new PostNotFoundException("Post not found!");

        this.likesRepository.deleteByUsernameAndPostId(username, postId);
    }

    public boolean isLikedByUser(Long postId, String username) {
        return this.likesRepository.existsByUsernameAndPostId(username, postId);
    }
}
