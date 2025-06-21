package org.example.authentication.business;

import org.example.authentication.exceptions.UserNotFoundException;
import org.example.authentication.models.User;
import org.example.authentication.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UsersServiceTests {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    private final User mockUser = new User("john", "John Doe", "john@example.com", "hashedpass");

    @Test
    void saveUser_shouldEncodePasswordAndSaveUser() {
        when(passwordEncoder.encode("plainpass")).thenReturn("hashedpass");

        usersService.saveUser("john", "John Doe", "john@example.com", "plainpass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("john", savedUser.getUsername());
        assertEquals("hashedpass", savedUser.getPassword());
    }

    @Test
    void getUser_shouldReturnUserIfFound() throws UserNotFoundException {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

        User result = usersService.getUser("john");

        assertEquals("john", result.getUsername());
    }

    @Test
    void getUser_shouldThrowIfUserNotFound() {
        when(usersRepository.findByUsername("nope")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> usersService.getUser("nope"));
    }

    @Test
    void updateUserProfileImage_shouldSetImageAndSave() throws UserNotFoundException {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

        usersService.updateUserProfileImage("john", "image123");

        assertEquals("image123", mockUser.getProfileImage());
        verify(usersRepository).save(mockUser);
    }

    @Test
    void updateUserBio_shouldSetBioAndSave() throws UserNotFoundException {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

        usersService.updateUserBio("john", "My bio");

        assertEquals("My bio", mockUser.getBio());
        verify(usersRepository).save(mockUser);
    }

    @Test
    void validateUserCredentials_shouldReturnTrueIfPasswordMatches() throws UserNotFoundException {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("rawpass", "hashedpass")).thenReturn(true);

        boolean result = usersService.validateUserCredentials("john", "rawpass");

        assertTrue(result);
    }

    @Test
    void validateUserCredentials_shouldReturnFalseIfPasswordDoesNotMatch() throws UserNotFoundException {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "hashedpass")).thenReturn(false);

        boolean result = usersService.validateUserCredentials("john", "wrong");

        assertFalse(result);
    }

    @Test
    void existsByUsername_shouldReturnTrueOrFalse() {
        when(usersRepository.existsByUsername("john")).thenReturn(true);
        assertTrue(usersService.existsByUsername("john"));

        when(usersRepository.existsByUsername("doe")).thenReturn(false);
        assertFalse(usersService.existsByUsername("doe"));
    }

    @Test
    void getAllUsernames_shouldReturnAllUsernames() {
        List<User> mockUsers = List.of(
                new User("john", "John Doe", "a@a.com", "x"),
                new User("alice", "Alice", "b@b.com", "y")
        );

        when(usersRepository.findAll()).thenReturn(mockUsers);

        List<String> usernames = usersService.getAllUsernames();
        assertEquals(List.of("john", "alice"), usernames);
    }

    @Test
    void getUsersByUsername_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 30);
        Page<User> mockPage = new PageImpl<>(List.of(mockUser));

        when(usersRepository.findByUsernameContainingIgnoreCase("jo", "john", pageable)).thenReturn(mockPage);

        Page<User> result = usersService.getUsersByUsername("jo", "john", 0);
        assertEquals(1, result.getTotalElements());
        assertEquals("john", result.getContent().get(0).getUsername());
    }
}
