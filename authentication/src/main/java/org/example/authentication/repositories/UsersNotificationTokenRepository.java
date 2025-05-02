package org.example.authentication.repositories;

import org.example.authentication.models.UserNotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersNotificationTokenRepository extends JpaRepository<UserNotificationToken, String> {
}
