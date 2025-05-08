package org.example.authentication.repositories;

import org.example.authentication.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query(value = """
            SELECT * FROM users
            WHERE LOWER(username) LIKE CONCAT('%', LOWER(:searchKey), '%') AND username <> :username
            """,
            countQuery = """
            SELECT COUNT(*) FROM users
            WHERE LOWER(username) LIKE CONCAT('%', LOWER(:searchKey), '%') AND username <> :username
            """,
            nativeQuery = true)
    Page<User> findByUsernameContainingIgnoreCase(String searchKey, String username, Pageable pageable);
}
