package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.CustomResponseException;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendWatchlistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private OTPService otpService;

    @InjectMocks
    private UserController userController;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request)); // Corrected line
    }

    // Helper method to create a User object
    private User createUser(Long id, String username, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    // Helper method to create a UserPostDTO object
    private UserPostDTO createUserPostDTO(String username, String password) {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(username);
        userPostDTO.setPassword(password);
        return userPostDTO;
    }

    @Test
    void testLoginUser_success() {
        // Arrange
        UserPostDTO userPostDTO = createUserPostDTO("testUser", "password");
        User loggedInUser = createUser(1L, "testUser", "encodedPassword");
        when(userService.loginUser("testUser", "password")).thenReturn(loggedInUser);
        doNothing().when(otpService).sendOTP("testUser"); // Use doNothing() for void methods

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = userController.loginUser(userPostDTO);
        Map<String, Object> response = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("OTP sent to your registered email.", response.get("message"));
        assertEquals(loggedInUser.getId(), response.get("userId"));
        verify(otpService, times(1)).sendOTP("testUser");
        verify(userService, times(1)).loginUser("testUser", "password");
    }

    @Test
    void testLoginUser_userLocked() {
        // Arrange
        UserPostDTO userPostDTO = createUserPostDTO("lockedUser", "password");
        Map<String, UserController.FailedLoginData> loginFailures = new HashMap<>();
        loginFailures.put("lockedUser", new UserController.FailedLoginData(5, LocalDateTime.now().plusMinutes(30)));

        // Use reflection to set the loginFailures
        try {
            java.lang.reflect.Field field = UserController.class.getDeclaredField("loginFailures");
            field.setAccessible(true);
            field.set(userController, loginFailures);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // Act
        CustomResponseException exception = assertThrows(CustomResponseException.class, () -> {
            userController.loginUser(userPostDTO);
        });

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus()); // Corrected to getHttpStatus()
        assertEquals("Too many failed attempts. Your account is locked. Please try again later.", exception.getMessage());
    }

    @Test
    void testLoginUser_invalidCredentials() {
        // Arrange
        UserPostDTO userPostDTO = createUserPostDTO("invalidUser", "wrongPassword");
        when(userService.loginUser("invalidUser", "wrongPassword")).thenThrow(new IllegalArgumentException("Invalid credentials"));

        // Act
        CustomResponseException exception = assertThrows(CustomResponseException.class, () -> {
            userController.loginUser(userPostDTO);
        });

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus()); // Corrected to getHttpStatus()
        assertEquals("Login failed: invalid username or password.", exception.getMessage());
    }

    @Test
    void testLoginUser_unexpectedError() {
        // Arrange
        UserPostDTO userPostDTO = createUserPostDTO("errorUser", "password");
        when(userService.loginUser("errorUser", "password")).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        CustomResponseException exception = assertThrows(CustomResponseException.class, () -> {
            userController.loginUser(userPostDTO);
        });

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus()); // Corrected to getHttpStatus()
        assertEquals("An unexpected error occurred. Please try again.", exception.getMessage());
    }

    @Test
    void testAddToWatchlist_missingMovieId() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("title", "Movie Title");
        payload.put("posterPath", "/path/to/poster.jpg");

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.addToWatchlist(1L, payload);
        });

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("movieId is required", exception.getReason());
    }

    @Test
    void testGetFriendsWatchlists() {
        // Arrange
        User friend1 = createUser(2L, "friend1", "password");
        friend1.setSharable(true);
        friend1.setWatchlist(Arrays.asList("movieA", "movieB"));

        User friend2 = createUser(3L, "friend2", "password");
        friend2.setSharable(false); // Not sharable

        User friend3 = createUser(4L, "friend3", "password");
        friend3.setSharable(true);
        friend3.setWatchlist(Collections.emptyList());

        List<User> friends = Arrays.asList(friend1, friend2, friend3);
        when(userService.getFriends(1L)).thenReturn(friends);

        // Act
        List<FriendWatchlistDTO> result = userController.getFriendsWatchlists(1L);

        // Assert
        assertEquals(2, result.size()); // Only 2 friends are sharable
        assertEquals(2L, result.get(0).getFriendId());
        assertEquals("friend1", result.get(0).getUsername());
        assertEquals(Arrays.asList("movieA", "movieB"), result.get(0).getWatchlist());
        assertEquals(4L, result.get(1).getFriendId());
        assertEquals("friend3", result.get(1).getUsername());
        assertEquals(Collections.emptyList(), result.get(1).getWatchlist());
    }

    @Test
    void testSendOTP_missingUsername() {
        // Arrange
        Map<String, String> payload = new HashMap<>();

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.sendOTP(payload);
        });

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Username is required to send OTP.", exception.getReason());
    }

    @Test
    void testVerifyOTP_missingUsername() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("otp", "123456");

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.verifyOTP(payload);
        });

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Username is required for OTP verification.", exception.getReason());
    }

    @Test
    void testVerifyOTP_missingOTP() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testUser");

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userController.verifyOTP(payload);
        });

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("OTP is required for verification.", exception.getReason());
    }

    @Test
    void testVerifyOTP_success() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testUser");
        payload.put("otp", "123456");
        HashMap<String, String> otpResponse = new HashMap<>(); // Change to HashMap
        otpResponse.put("message", "OTP verified successfully");
        otpResponse.put("token", "OTP verified successfully for username: testUser");
        otpResponse.put("userId", "1");
        when(otpService.verifyOTP(anyString(), anyString())).thenReturn(otpResponse);

        // Act
        ResponseEntity<Map<String, String>> responseEntity = userController.verifyOTP(payload);
        Map<String, String> response = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("OTP verified successfully", response.get("message"));
    }

    @Test
    void testVerifyOTP_invalidOTP() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testUser");
        payload.put("otp", "invalid");
        when(otpService.verifyOTP("testUser", "invalid")).thenThrow(new IllegalArgumentException("Invalid OTP"));

        // Act
        ResponseEntity<Map<String, String>> responseEntity = userController.verifyOTP(payload);
        Map<String, String> response = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Invalid OTP", response.get("error"));
    }

    @Test
    void testVerifyOTP_unexpectedError() {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testUser");
        payload.put("otp", "error");
        when(otpService.verifyOTP("testUser", "error")).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Map<String, String>> responseEntity = userController.verifyOTP(payload);
        Map<String, String> response = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("An unexpected error occurred during OTP verification.", response.get("error"));
    }
}

