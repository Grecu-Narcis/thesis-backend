package org.example.postsservice.business;

import org.example.postsservice.dto.HeatMapPostDTO;
import org.example.postsservice.exceptions.AddPostException;
import org.example.postsservice.exceptions.AlreadyLikedPostException;
import org.example.postsservice.exceptions.PostNotFoundException;
import org.example.postsservice.models.Post;
import org.example.postsservice.models.likes.Like;
import org.example.postsservice.repositories.LikesRepository;
import org.example.postsservice.repositories.PostsRepository;
import org.example.postsservice.utils.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final PostsNotificationService postsNotificationService;

    static int SRID = 4326;
    static int pageSize = 30;

    @Autowired
    public PostsService(PostsRepository postsRepository, LikesRepository likesRepository,
                        PostsNotificationService postsNotificationService) {
        this.postsRepository = postsRepository;
        this.likesRepository = likesRepository;
        this.postsNotificationService = postsNotificationService;
        this.geometryFactory = new GeometryFactory();
    }

    // TODO: Add post validator
    public Post addPost(String imageKey, String createdBy, String description, double latitude, double longitude,
                        String carBrand, String carModel, int productionYear) throws AddPostException {
        try {
            Point postLocation = this.geometryFactory.createPoint(new Coordinate(longitude, latitude));
            postLocation.setSRID(PostsService.SRID);

            Post postToAdd = new Post(imageKey, createdBy, description, postLocation, carBrand, carModel, productionYear);

            return this.postsRepository.save(postToAdd);
        }
        catch (Exception e) {
            Logger.logError("Failed to add post: " + e.getMessage());
            throw new AddPostException("Failed to add post!");
        }
    }

    public Post getById(Long postId) throws Exception {
        Optional<Post> post = this.postsRepository.findById(postId);

        if (post.isEmpty())
            throw new Exception("Post not found!");

        return post.get();
    }

    public Page<Post> findNearbyPosts(String username, double latitude, double longitude, int pageNumber) {
        String pointWKT = String.format(Locale.US, "POINT(%f %f)", latitude, longitude);
        Pageable pageable = PageRequest.of(pageNumber, PostsService.pageSize);

        return this.postsRepository.findPostsNearbyUser(pointWKT, username, pageable);
    }

    public Page<Post> findPostsByFollowedUsers(String username, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PostsService.pageSize);

        return this.postsRepository.findPostsByFollowedUsers(username, pageable);
    }

    public Page<Post> findPostsByUsername(String username, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PostsService.pageSize, Sort.by("createdAt").descending());

        return this.postsRepository.findPostsByCreatedBy(username, pageable);
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
        this.postsNotificationService.notifyNewLike(postId, username, post.getCreatedBy());
    }

    @Transactional
    public void unlikePost(Long postId, String username) throws PostNotFoundException {
        Optional<Post> requiredPost = this.postsRepository.findById(postId);

        if (requiredPost.isEmpty()) throw new PostNotFoundException("Post not found!");

        Post post = requiredPost.get();
        post.setLikesCount(post.getLikesCount() - 1);
        this.postsRepository.save(post);
        this.likesRepository.deleteByUsernameAndPostId(username, postId);
    }

    public boolean isLikedByUser(Long postId, String username) {
        return this.likesRepository.existsByUsernameAndPostId(username, postId);
    }

    public int countPostsByUser(String username) {
        return this.postsRepository.countByCreatedBy(username);
    }

    public List<HeatMapPostDTO> getPostsForHeatMap(double minLat, double maxLat, double minLon, double maxLon) {
        return this.postsRepository.findPostsForHeatMap(minLat, maxLat, minLon, maxLon);
    }
}
