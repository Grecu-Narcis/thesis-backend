package org.example.authentication.business;

import org.example.authentication.exceptions.UserNotFoundException;
import org.example.authentication.models.User;
import org.example.authentication.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUser_shouldHashPasswordAndSaveUser() {
        String username = "testUser";
        String fullName = "Test User";
        String email = "test@example.com";
        String password = "password";
        String hashedPassword = "hashed";

        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

        usersService.saveUser(username, fullName, email, password);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(username, savedUser.getUsername());
        assertEquals(fullName, savedUser.getFullName());
        assertEquals(email, savedUser.getEmail());
        assertEquals(hashedPassword, savedUser.getPassword());
    }

    @Test
    void validateUserCredentials_shouldReturnTrueForMatchingPassword() throws UserNotFoundException {
        String username = "testUser";
        String password = "password";
        String hashedPassword = "hashed";

        User user = new User(username, "Test User", "test@example.com", hashedPassword);

        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        assertTrue(usersService.validateUserCredentials(username, password));
    }

    @Test
    void validateUserCredentials_shouldThrowExceptionIfUserNotFound() {
        when(usersRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            usersService.validateUserCredentials("nonexistent", "password");
        });
    }

    @Test
    void existsByUsername_shouldReturnTrueIfUserExists() {
        when(usersRepository.existsByUsername("existingUser")).thenReturn(true);

        assertTrue(usersService.existsByUsername("existingUser"));
    }

    @Test
    void getAllUsernames_shouldReturnAllUsernames() {
        List<User> users = List.of(
                new User("user1", "User One", "u1@example.com", "pass1"),
                new User("user2", "User Two", "u2@example.com", "pass2")
        );

        when(usersRepository.findAll()).thenReturn(users);

        List<String> usernames = usersService.getAllUsernames();

        assertEquals(List.of("user1", "user2"), usernames);
    }

    @Test
    void getUsersByUsername_shouldReturnPageOfUsers() {
        String searchKey = "john";
        String excludeUsername = "johnny";
        int page = 0;
        Pageable pageable = PageRequest.of(page, 30);
        Page<User> pageOfUsers = new PageImpl<>(List.of(
                new User("johnsmith", "John Smith", "js@example.com", "pass")
        ));

        when(usersRepository.findByUsernameContainingIgnoreCase(searchKey, excludeUsername, pageable)).thenReturn(pageOfUsers);

        Page<User> result = usersService.getUsersByUsername(searchKey, excludeUsername, page);

        assertEquals(1, result.getContent().size());
        assertEquals("johnsmith", result.getContent().get(0).getUsername());
    }
}