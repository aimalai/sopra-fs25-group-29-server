package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserPersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Rollback(false) // Ensures changes are committed to the database for testing
    public void testInsertUser() {
        // Create and save a new user
        User newUser = new User();
        newUser.setUsername("integration_user");
        newUser.setPassword("integration_password");
        newUser.setFailedLoginAttempts(0);
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(newUser);

        // Retrieve the user from the database
        Optional<User> fetchedUser = userRepository.findByUsername("integration_user");

        // Assertions
        assertTrue(fetchedUser.isPresent(), "User should exist in the database");
        assertEquals("integration_user", fetchedUser.get().getUsername(), "Username should match");
        assertEquals(0, fetchedUser.get().getFailedLoginAttempts(), "Failed login attempts should be 0");
    }

    @Test
    @Rollback(false)
    public void testUpdateFailedLoginAttempts() {
        // Insert a user
        User user = new User();
        user.setUsername("update_user");
        user.setPassword("update_password");
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Retrieve and update failed login attempts
        Optional<User> fetchedUser = userRepository.findByUsername("update_user");
        assertTrue(fetchedUser.isPresent(), "User should exist in the database");

        User existingUser = fetchedUser.get();
        existingUser.setFailedLoginAttempts(2);
        userRepository.save(existingUser);

        // Verify the update
        Optional<User> updatedUser = userRepository.findByUsername("update_user");
        assertTrue(updatedUser.isPresent(), "User should exist in the database after update");
        assertEquals(2, updatedUser.get().getFailedLoginAttempts(), "Failed login attempts should be updated to 2");
    }
}
