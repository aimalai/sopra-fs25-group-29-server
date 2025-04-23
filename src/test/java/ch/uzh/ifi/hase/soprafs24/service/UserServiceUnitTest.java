package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
