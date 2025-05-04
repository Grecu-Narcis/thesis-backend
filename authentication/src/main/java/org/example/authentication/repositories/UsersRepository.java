package org.example.authentication.repositories;

import org.example.authentication.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameContainingIgnoreCase(String searchKey, Pageable pageable);
    int countByUsernameContainingIgnoreCase(String searchKey);
}
