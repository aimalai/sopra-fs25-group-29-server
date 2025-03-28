package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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

        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("StrongPass@123");
        testUser.setEmail("test@example.com");

        // Mocking user repository behavior
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    public void registerUser_validInputs_success() {
        // when -> simulate saving user in repository
        User registeredUser = userService.registerUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals(testUser.getId(), registeredUser.getId());
        assertEquals(testUser.getUsername(), registeredUser.getUsername());
        assertEquals(testUser.getEmail(), registeredUser.getEmail());
        assertNotNull(registeredUser.getCreatedAt());
    }

    @Test
    public void registerUser_duplicateUsername_throwsException() {
        // given -> a first user with the same username already exists
        Mockito.when(userRepository.existsByUsername(Mockito.any())).thenReturn(true);

        // then -> expect exception on attempt to register user
        assertThrows(ResponseStatusException.class, () -> userService.registerUser(testUser));
    }

    @Test
    public void loginUser_invalidCredentials_throwsException() {
        // given -> no matching user found in repository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());

        // then -> expect exception when logging in
        assertThrows(ResponseStatusException.class, () -> userService.login(testUser.getUsername(), testUser.getPassword()));
    }

    @Test
    public void loginUser_validCredentials_success() {
        // given -> matching user found in repository
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.of(testUser));

        // when -> simulate successful login
        User loggedInUser = userService.login(testUser.getUsername(), testUser.getPassword());

        // then
        assertEquals(testUser.getId(), loggedInUser.getId());
        assertEquals(testUser.getUsername(), loggedInUser.getUsername());
    }
}
