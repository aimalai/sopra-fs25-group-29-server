package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.CustomResponseException;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest2 {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void loginUser_validCredentials_success() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        // When
        User loggedInUser = userService.loginUser("testUsername", "User.1234");

        // Then
        assertNotNull(loggedInUser.getToken());
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    }

    @Test
    public void loginUser_invalidCredentials_throwsException() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        // When & Then
        assertThrows(CustomResponseException.class, () -> userService.loginUser("testUsername", "wrongPassword"));
    }

    @Test
    public void logoutUser_changesStatusToOffline() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        User createdUser = userService.createUser(testUser);

        // When
        userService.logoutUser(createdUser.getId());

        // Then
        User reloadedUser = userService.getUserById(createdUser.getId());
        assertEquals(UserStatus.OFFLINE, reloadedUser.getStatus());
    }
}
