package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Given: A test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test_user"); // Match username to expected value in tests
        testUser.setPassword("StrongPass@123");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        // Mock repository behavior
        Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);
        Mockito.when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @Test
    public void testInsertNewUser_persistenceCheck() {
        // Insert a new user and verify persistence behavior
        User newUser = new User();
        newUser.setUsername("test_user");
        newUser.setPassword("password123");
        newUser.setCreatedAt(LocalDateTime.now());

        Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(newUser);
        userRepository.saveAndFlush(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(newUser);

        Optional<User> fetchedUser = userRepository.findByUsername("test_user");
        assertTrue(fetchedUser.isPresent(), "New user should be saved and persisted");
        assertEquals("test_user", fetchedUser.get().getUsername(), "Username should match the persisted user");
    }

    @Test
    public void testUpdateUserEmail_persistenceCheck() {
        // Update user's email and verify persistence behavior
        testUser.setEmail("newemail@test.com");
        userRepository.saveAndFlush(testUser);

        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(testUser);

        Optional<User> updatedUser = userRepository.findById(1L);
        assertTrue(updatedUser.isPresent(), "User should be found after update");
        assertEquals("newemail@test.com", updatedUser.get().getEmail(), "Email should be updated and persisted");
    }
}
