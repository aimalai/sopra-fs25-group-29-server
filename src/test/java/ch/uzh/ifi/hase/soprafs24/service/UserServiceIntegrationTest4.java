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
public class UserServiceIntegrationTest4 {

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
        testUser.setWatchlist(new ArrayList<>());
        testUser.setIncomingFriendRequests(new HashSet<>());
        testUser.setFriends(new HashSet<>());
    }

    // Test: updateUser - valid inputs
    @Test
    public void updateUser_validInputs_success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User userData = new User();
        userData.setUsername("updatedUsername");
        userData.setEmail("updated@example.com");

        // When
        userService.updateUser(1L, userData);

        // Then
        verify(userRepository, times(1)).save(testUser);
        assertEquals("updatedUsername", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
    }

    // Test: updateUser - user not found
    @Test
    public void updateUser_userNotFound_throwsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        User userData = new User();

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, userData));
    }

    // Test: acceptFriendRequest - valid scenario
    @Test
    public void acceptFriendRequest_validScenario_success() {
        // Given
        User sender = new User();
        sender.setId(2L);
        sender.setUsername("senderUsername");
        sender.setFriends(new HashSet<>());

        testUser.setIncomingFriendRequests(new HashSet<>(Collections.singletonList(2L)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sender));

        // When
        userService.acceptFriendRequest(1L, 2L);

        // Then
        assertTrue(testUser.getFriends().contains(2L));
        assertFalse(testUser.getIncomingFriendRequests().contains(2L));
        verify(userRepository, times(1)).save(testUser);
        verify(userRepository, times(1)).save(sender);
        assertTrue(sender.getFriends().contains(1L));
    }

    // Test: acceptFriendRequest - no incoming request
    @Test
    public void acceptFriendRequest_noIncomingRequest_throwsException() {
        // Given
        testUser.setIncomingFriendRequests(new HashSet<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.acceptFriendRequest(1L, 2L));
    }

    // Test: declineFriendRequest - valid scenario
    @Test
    public void declineFriendRequest_validScenario_success() {
        // Given
        testUser.setIncomingFriendRequests(new HashSet<>(Collections.singletonList(2L)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.declineFriendRequest(1L, 2L);

        // Then
        assertFalse(testUser.getIncomingFriendRequests().contains(2L));
        verify(userRepository, times(1)).save(testUser);
    }

    // Test: declineFriendRequest - no incoming request
    @Test
    public void declineFriendRequest_noIncomingRequest_throwsException() {
        // Given
        testUser.setIncomingFriendRequests(new HashSet<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.declineFriendRequest(1L, 2L));
    }

    // Parameterized Test: addMovieToWatchlist
    @Test
    public void addMovieToWatchlist_validMovies_success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        String movie1 = "{\"movieId\":\"1\",\"title\":\"Movie Title 1\"}";
        String movie2 = "{\"movieId\":\"2\",\"title\":\"Movie Title 2\"}";

        // When
        userService.addMovieToWatchlist(1L, movie1);
        userService.addMovieToWatchlist(1L, movie2);

        // Then
        assertEquals(2, testUser.getWatchlist().size());
        assertTrue(testUser.getWatchlist().contains(movie1));
        assertTrue(testUser.getWatchlist().contains(movie2));
        verify(userRepository, times(2)).save(testUser);
    }

    // Test: addMovieToWatchlist - duplicate movie
    @Test
    public void addMovieToWatchlist_duplicateMovie_doesNotAdd() {
        // Given
        String movie = "{\"movieId\":\"1\",\"title\":\"Movie Title\"}";
        testUser.getWatchlist().add(movie);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.addMovieToWatchlist(1L, movie);

        // Then
        assertEquals(1, testUser.getWatchlist().size());
        verify(userRepository, times(0)).save(testUser);
    }
}
