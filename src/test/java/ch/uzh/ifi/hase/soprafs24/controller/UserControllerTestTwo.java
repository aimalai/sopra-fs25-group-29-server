package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserControllerTestTwo {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogoutWithMissingToken() {
        // Act: Call logout with a missing token (null value)
        ResponseEntity<?> response = userController.logoutUser(null);

        // Assert: Verify response is 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing token.", response.getBody());
    }

    @Test
    public void testLogoutWithMalformedToken() {
        // Arrange: Mock behavior for malformed token
        doThrow(new IllegalArgumentException("Malformed token.")).when(userService).logout(anyString());

        // Act: Call logout with a malformed token
        ResponseEntity<?> response = userController.logoutUser("malformedToken");

        // Assert: Verify response is 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed token.", response.getBody());
    }

    @Test
    public void testLogoutWithExpiredToken() {
        // Arrange: Mock behavior for expired token
        doThrow(new IllegalArgumentException("Token expired.")).when(userService).logout(anyString());

        // Act: Call logout with an expired token
        ResponseEntity<?> response = userController.logoutUser("expiredToken");

        // Assert: Verify response is 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Token expired.", response.getBody());
    }
}
