package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.util.List;

@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest3 {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void addMovieToWatchlist_validMovie_success() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        // When
        userService.addMovieToWatchlist(testUser.getId(), "{\"movieId\":\"1\",\"title\":\"Movie Title\"}");

        // Then
        List<String> watchlist = userService.getWatchlist(testUser.getId());
        assertEquals(1, watchlist.size());
        assertEquals("{\"movieId\":\"1\",\"title\":\"Movie Title\"}", watchlist.get(0));
    }

    @Test
    public void addMovieToWatchlist_duplicateMovie_doesNotAdd() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);
        userService.addMovieToWatchlist(testUser.getId(), "{\"movieId\":\"1\",\"title\":\"Movie Title\"}");

        // When
        userService.addMovieToWatchlist(testUser.getId(), "{\"movieId\":\"1\",\"title\":\"Movie Title\"}");

        // Then
        List<String> watchlist = userService.getWatchlist(testUser.getId());
        assertEquals(1, watchlist.size()); // Duplicate movie was not added
    }

    @Test
    public void getWatchlist_userWithoutMovies_returnsEmptyList() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);

        // When
        List<String> watchlist = userService.getWatchlist(testUser.getId());

        // Then
        assertTrue(watchlist.isEmpty());
    }

    @Test
    public void removeMovieFromWatchlist_validMovieId_success() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);
        userService.addMovieToWatchlist(testUser.getId(), "{\"movieId\":\"1\",\"title\":\"Movie Title\"}");

        // When
        userService.removeMovieFromWatchlist(testUser.getId(), "1");

        // Then
        List<String> watchlist = userService.getWatchlist(testUser.getId());
        assertTrue(watchlist.isEmpty());
    }

    @Test
    public void removeMovieFromWatchlist_invalidMovieId_doesNothing() {
        // Given
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("User.1234");
        testUser.setEmail("test@example.com");
        userService.createUser(testUser);
        userService.addMovieToWatchlist(testUser.getId(), "{\"movieId\":\"1\",\"title\":\"Movie Title\"}");

        // When
        userService.removeMovieFromWatchlist(testUser.getId(), "2"); // Invalid movieId

        // Then
        List<String> watchlist = userService.getWatchlist(testUser.getId());
        assertEquals(1, watchlist.size()); // Watchlist is unchanged
    }
}
