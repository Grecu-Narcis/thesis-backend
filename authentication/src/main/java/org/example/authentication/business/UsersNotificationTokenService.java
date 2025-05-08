package org.example.authentication.business;

import org.example.authentication.exceptions.TokenNotFoundException;
import org.example.authentication.models.UserNotificationToken;
import org.example.authentication.repositories.UsersNotificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersNotificationTokenService {
    private final UsersNotificationTokenRepository usersNotificationTokenRepository;

    @Autowired
    public UsersNotificationTokenService(UsersNotificationTokenRepository usersNotificationTokenRepository) {
        this.usersNotificationTokenRepository = usersNotificationTokenRepository;
    }

    public void saveUserNotificationToken(UserNotificationToken userNotificationToken) {
        usersNotificationTokenRepository.save(userNotificationToken);
    }

    public String getUserNotificationToken(String username) throws TokenNotFoundException {
        Optional<UserNotificationToken> tokenOptional = usersNotificationTokenRepository.findById(username);

        if (tokenOptional.isEmpty()) throw  new TokenNotFoundException("Token not found!");

        return tokenOptional.get().getToken();
    }
}
