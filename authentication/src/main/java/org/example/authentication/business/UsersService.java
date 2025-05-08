package org.example.authentication.business;

import org.example.authentication.exceptions.UserNotFoundException;
import org.example.authentication.models.User;
import org.example.authentication.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    static int pageSize = 30;

    @Autowired
    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(String username, String fullName, String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, fullName, email, hashedPassword);
        usersRepository.save(user);
    }

    private User getUser(String username) throws UserNotFoundException {
        Optional<User> user = usersRepository.findByUsername(username);

        if (user.isEmpty())
            throw new UserNotFoundException("User not found!");

        return user.get();
    }

    public boolean validateUserCredentials(String username, String password) throws UserNotFoundException {
        User requiredUser = this.getUser(username);

        return passwordEncoder.matches(password, requiredUser.getPassword());
    }

    public boolean existsByUsername(String username) {
        return usersRepository.existsByUsername(username);
    }

    public List<String> getAllUsernames() {
        return this.usersRepository.findAll().stream().map(User::getUsername).toList();
    }

    public Page<User> getUsersByUsername(String searchKey, String username, int page) {
        Pageable pageable = PageRequest.of(page, UsersService.pageSize);
        return usersRepository.findByUsernameContainingIgnoreCase(searchKey, username, pageable);
    }
}
