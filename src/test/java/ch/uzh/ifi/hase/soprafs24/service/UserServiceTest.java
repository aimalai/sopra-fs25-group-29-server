package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.utils.ImageUploadUtil;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MultipartFile file;
    @InjectMocks private UserService userService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("test@example.com");
        mockUser.setProfilePictureUrl("oldPicUrl");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void testUploadProfilePicture_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> userService.uploadProfilePicture(2L, file));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void testUpdateUser_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> userService.updateUser(2L, new User()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void testSendFriendRequest_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> userService.sendFriendRequest(2L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Target user not found", exception.getReason());
    }

    @Test
    void testAcceptFriendRequest_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> userService.acceptFriendRequest(2L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Target user not found", exception.getReason());
    }
}
