package org.example.postsservice.repositories;

import org.example.postsservice.models.seen_by.SeenBy;
import org.example.postsservice.models.seen_by.SeenById;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeenByRepository extends JpaRepository<SeenBy, SeenById> {
}
