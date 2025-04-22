package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.util.*;

@SpringBootTest
public class UserServiceIntegrationTest5 {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setEmail("test@example.com");
        testUser.setPassword("User.1234");
        testUser.setIncomingFriendRequests(new HashSet<>());
        testUser.setFriends(new HashSet<>());
    }

    // Test: getFriendRequests - valid user
    @Test
    public void getFriendRequests_validUser_returnsRequests() {
        // Given
        testUser.setIncomingFriendRequests(new HashSet<>(Arrays.asList(2L, 3L)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        List<Long> friendRequests = userService.getFriendRequests(1L);

        // Then
        assertEquals(2, friendRequests.size());
        assertTrue(friendRequests.contains(2L));
        assertTrue(friendRequests.contains(3L));
    }

    // Test: getFriendRequests - user not found
    @Test
    public void getFriendRequests_userNotFound_throwsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.getFriendRequests(1L));
    }

    // Test: getFriends - valid user with friends
    @Test
    public void getFriends_validUser_returnsFriends() {
        // Given
        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friendUsername");
        testUser.getFriends().add(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));

        // When
        List<User> friends = userService.getFriends(1L);

        // Then
        assertEquals(1, friends.size());
        assertEquals("friendUsername", friends.get(0).getUsername());
    }

    // Test: getFriends - user not found
    @Test
    public void getFriends_userNotFound_throwsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.getFriends(1L));
    }

    // Test: areFriends - valid friendship
    @Test
    public void areFriends_validFriendship_returnsTrue() {
        // Given
        testUser.getFriends().add(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.areFriends(1L, 2L);

        // Then
        assertTrue(result);
    }

    // Test: areFriends - no friendship
    @Test
    public void areFriends_noFriendship_returnsFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.areFriends(1L, 3L);

        // Then
        assertFalse(result);
    }

    // Test: areFriends - user not found
    @Test
    public void areFriends_userNotFound_returnsFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        boolean result = userService.areFriends(1L, 2L);

        // Then
        assertFalse(result);
    }
}
