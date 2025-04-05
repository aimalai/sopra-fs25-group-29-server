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

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogoutWithValidToken() {
        // Arrange: Mock the valid token
        String validToken = "validToken";

        // Act: Call logout with the valid token
        ResponseEntity<?> response = userController.logoutUser(validToken);

        // Assert: Verify userService.logout is called and response is 200 OK
        verify(userService, times(1)).logout(validToken);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful.", response.getBody());
    }

    @Test
    public void testLogoutWithInvalidToken() {
        // Arrange: Mock an invalid token
        doThrow(new RuntimeException("Invalid token.")).when(userService).logout(anyString());

        // Act: Call logout with the invalid token
        ResponseEntity<?> response = userController.logoutUser("invalidToken");

        // Assert: Verify appropriate error response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred during logout.", response.getBody());
    }
}
