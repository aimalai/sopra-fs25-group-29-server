package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserServiceTestTwo {

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTokenBlacklisting() {
        // Arrange: Create a dummy user
        User dummyUser = new User();
        dummyUser.setUsername("testUser");

        // Generate a token for the dummy user
        String token = userService.generateToken(dummyUser);

        // Act: Blacklist the token
        userService.logout(token);

        // Assert: Verify that the token is blacklisted
        assertFalse(userService.isTokenValid(token), "Blacklisted token should be invalid.");
    }
}
