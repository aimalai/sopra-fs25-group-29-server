package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

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
        testUser.setEmail("test@example.com"); 

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
        testUser.setEmail("test@example.com"); 
        User createdUser = userService.createUser(testUser);

        // attempt to create second user with same username
        User testUser2 = new User();
        testUser2.setPassword("User.1234");
        testUser2.setUsername("testUsername");
        testUser2.setEmail("duplicate@example.com"); 

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

    @Test
    public void removeFriend_shouldRemoveFriendship() {
        User u1 = new User();
        u1.setUsername("u1");
        u1.setEmail("u1@x");
        u1.setPassword("p");
        User saved1 = userService.createUser(u1);
        User u2 = new User();
        u2.setUsername("u2");
        u2.setEmail("u2@x");
        u2.setPassword("p");
        User saved2 = userService.createUser(u2);
        saved1.getFriends().add(saved2.getId());
        saved2.getFriends().add(saved1.getId());
        userRepository.save(saved1);
        userRepository.save(saved2);
        userService.removeFriend(saved1.getId(), saved2.getId());
        User reloaded1 = userRepository.findById(saved1.getId()).get();
        User reloaded2 = userRepository.findById(saved2.getId()).get();
        assertEquals(false, reloaded1.getFriends().contains(saved2.getId()));
        assertEquals(false, reloaded2.getFriends().contains(saved1.getId()));
    }

    @Test
    public void declineFriendRequest_removesRequest() {
        User target = new User();
        target.setUsername("target"); target.setEmail("t@e"); target.setPassword("p");
        User from   = new User();
        from.setUsername("from");   from.setEmail("f@e"); from.setPassword("p");

        User savedTarget = userService.createUser(target);
        User savedFrom   = userService.createUser(from);

        userService.sendFriendRequest(savedTarget.getId(), savedFrom.getId());
        userService.declineFriendRequest(savedTarget.getId(), savedFrom.getId());

        List<Long> requests = userService.getFriendRequests(savedTarget.getId());
        assertFalse(requests.contains(savedFrom.getId()));
    }

    @Test
    public void declineFriendRequest_whenNoRequest_throwsException() {
        assertThrows(ResponseStatusException.class, () ->
            userService.declineFriendRequest(999L, 888L)
        );
    }
}
