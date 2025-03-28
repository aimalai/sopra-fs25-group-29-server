package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserService REST resource.
 *
 * @see UserService
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
    public void registerUser_validInputs_success() {
        // given
        assertTrue(userRepository.findByUsername("testUsername").isEmpty());

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("StrongPass@123");
        testUser.setEmail("test@example.com");

        // when
        User registeredUser = userService.registerUser(testUser);

        // then
        Optional<User> retrievedUser = userRepository.findByUsername("testUsername");
        assertTrue(retrievedUser.isPresent());
        assertEquals(registeredUser.getId(), retrievedUser.get().getId());
        assertEquals("testUsername", retrievedUser.get().getUsername());
        assertEquals("test@example.com", retrievedUser.get().getEmail());
        assertNotNull(retrievedUser.get().getCreatedAt());
    }

    @Test
    public void registerUser_duplicateUsername_throwsException() {
        // given
        assertTrue(userRepository.findByUsername("testUsername").isEmpty());

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("StrongPass@123");
        testUser.setEmail("test@example.com");
        userService.registerUser(testUser);

        // attempt to register second user with same username
        User testUser2 = new User();
        testUser2.setUsername("testUsername");
        testUser2.setPassword("AnotherStrongPass@123");
        testUser2.setEmail("test2@example.com");

        // then -> Expect exception
        assertThrows(ResponseStatusException.class, () -> userService.registerUser(testUser2));
    }
}
