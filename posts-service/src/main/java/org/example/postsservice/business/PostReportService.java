package org.example.postsservice.business;

import org.example.postsservice.models.post_report.PostReport;
import org.example.postsservice.repositories.PostReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostReportService {
    private final PostReportRepository postReportRepository;


    /**
     * Constructor-based dependency injection for repositories.
     * @param postReportRepository Repository for PostReport entities.
     */
    public PostReportService(PostReportRepository postReportRepository) {
        this.postReportRepository = postReportRepository;
    }

    /**
     * Creates and saves a new report for a post by a specific user.
     * The method is transactional, so all operations within it either
     * succeed together or fail together.
     *
     * @param postId The ID of the post to be reported.
     * @param username The username of the user creating the report.
     * @return The saved PostReport entity.
     */
    @Transactional
    public PostReport createPostReport(Long postId, String username) {
        PostReport newReport = new PostReport(postId, username);

        return postReportRepository.save(newReport);
    }
}
