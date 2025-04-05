package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTestThree {

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSessionReusePrevention() {
        // Arrange: Create a dummy user and generate a token
        User dummyUser = new User();
        dummyUser.setUsername("testUser");
        String token = userService.generateToken(dummyUser);

        // Act: Blacklist the token by logging out
        userService.logout(token);

        // Assert: Attempt to reuse the token should throw an exception
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> {
                // Simulate request processing with token validation
                if (!userService.isTokenValid(token)) {
                    throw new RuntimeException("Access denied for invalid token!");
                }
            }
        );

        // Validate the exception message
        assertFalse(exception.getMessage().isEmpty(), "Access should be denied for invalid tokens.");
    }
}
