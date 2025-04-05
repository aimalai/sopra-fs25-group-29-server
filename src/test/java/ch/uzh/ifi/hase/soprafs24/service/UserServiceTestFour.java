package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTestFour {

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTokenValidation() {
        // Arrange: Create a dummy user and generate a token
        User dummyUser = new User();
        dummyUser.setUsername("testUser");
        String validToken = userService.generateToken(dummyUser);

        // Act: Validate the token (should be valid initially)
        boolean isValidBeforeLogout = userService.isTokenValid(validToken);

        // Blacklist the token by logging out
        userService.logout(validToken);

        // Validate the token again (should now be invalid)
        boolean isValidAfterLogout = userService.isTokenValid(validToken);

        // Assert: Verify token is initially valid and then invalid after logout
        assertTrue(isValidBeforeLogout, "Token should be valid before logout.");
        assertFalse(isValidAfterLogout, "Token should be invalid after logout.");
    }
}
