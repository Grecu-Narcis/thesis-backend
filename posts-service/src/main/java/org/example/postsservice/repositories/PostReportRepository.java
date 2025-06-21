package org.example.postsservice.repositories;

import org.example.postsservice.models.post_report.PostReport;
import org.example.postsservice.models.post_report.PostReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, PostReportId> {
}
