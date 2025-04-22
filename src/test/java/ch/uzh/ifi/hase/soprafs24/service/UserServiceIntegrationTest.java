package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

/**
 * Integration tests for UserService covering various scenarios.
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

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
    public void createUser_validInputs_success() {
        // given
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setPassword("User.1234");
        testUser.setUsername("testUsername");
        testUser.setEmail("test@example.com"); // Added email field

        // when
        User createdUser = userService.createUser(testUser);

        // then
        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getPassword(), createdUser.getPassword());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertEquals(testUser.getEmail(), createdUser.getEmail()); // Verify email field
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setPassword("User.1234");
        testUser.setUsername("testUsername");
        testUser.setEmail("test@example.com"); // Added email field
        User createdUser = userService.createUser(testUser);

        // attempt to create second user with same username
        User testUser2 = new User();
        testUser2.setPassword("User.1234");
        testUser2.setUsername("testUsername");
        testUser2.setEmail("duplicate@example.com"); // Added email field

        // check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
    }

    @Test
    public void createUser_missingEmail_throwsException() {
        User testUser = new User();
        testUser.setPassword("User.1234");
        testUser.setUsername("testUsername");
        assertThrows(ResponseStatusException.class,
                () -> userService.createUser(testUser));
    }

    @Test
    public void createUser_duplicateEmail_throwsException() {
        User testUser1 = new User();
        testUser1.setPassword("User.1234");
        testUser1.setUsername("testUsername");
        testUser1.setEmail("test@example.com");
        userService.createUser(testUser1);

        User testUser2 = new User();
        testUser2.setPassword("User.1234");
        testUser2.setUsername("testUsername");
        testUser2.setEmail("test@example.com");

        assertThrows(ResponseStatusException.class,
                () -> userService.createUser(testUser2));
    }

    @Test
    public void loginUser_validCredentials_success() {
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        User loggedIn = userService.loginUser("testUsername", "User.1234");
        assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
    }

    @Test
    public void loginUser_unknownUser_throwsException() {
        assertThrows(CustomResponseException.class,
                () -> userService.loginUser("testUsername", "User.1234"));
    }

    @Test
    public void loginUser_wrongPassword_throwsException() {
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        assertThrows(CustomResponseException.class,
                () -> userService.loginUser("testUsername", "WrongPassword"));
    }

    @Test
    public void logoutUser_setsStatusOffline() {
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        User created = userService.createUser(testUser);

        userService.logoutUser(created.getId());
        User reloaded = userService.getUserById(created.getId());
        assertEquals(UserStatus.OFFLINE, reloaded.getStatus());
    }
}
