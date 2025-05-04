package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock(lenient = true)
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setWatchlist(new ArrayList<>(List.of("{\"movieId\":\"1\"}", "{\"movieId\":\"2\"}")));
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));
    }

    @Test
    void removeMovieFromWatchlist_removesOnlyMatchingEntries() {
        userService.removeMovieFromWatchlist(1L, "1");
        assertEquals(1, user.getWatchlist().size());
        verify(userRepository).save(user);
    }

    @Test
    void removeMovieFromWatchlist_userNotFound_throws404() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            userService.removeMovieFromWatchlist(2L, "1")
        );
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void removeFriend_removesFriendshipFromBothSides() {
        User alice = new User();
        alice.setId(1L);
        alice.getFriends().add(2L);
        User bob = new User();
        bob.setId(2L);
        bob.getFriends().add(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        userService.removeFriend(1L, 2L);
        assertFalse(alice.getFriends().contains(2L));
        assertFalse(bob.getFriends().contains(1L));
        verify(userRepository).save(alice);
        verify(userRepository).save(bob);
    }

    @Test
    void declineFriendRequest_removesFromIncomingRequests() {
        user.getIncomingFriendRequests().add(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.declineFriendRequest(1L, 2L);

        assertFalse(user.getIncomingFriendRequests().contains(2L));
        verify(userRepository).save(user);
    }

    @Test
    void declineFriendRequest_noRequest_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> userService.declineFriendRequest(1L, 2L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

}
