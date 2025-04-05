package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTokenInvalidationOnLogout() {
        // Arrange: Create a dummy user
        User dummyUser = new User();
        dummyUser.setUsername("testUser");

        // Generate a token for the dummy user
        String token = userService.generateToken(dummyUser);

        // Act: Logout by invalidating the token
        userService.logout(token);

        // Assert: Verify the token is no longer valid
        assertFalse(userService.isTokenValid(token), "The token should be invalid after logout.");
    }
}
