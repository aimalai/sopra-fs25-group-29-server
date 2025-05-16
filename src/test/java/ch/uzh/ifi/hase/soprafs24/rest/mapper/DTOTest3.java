package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendWatchlistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TopRatedMovieDTO;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DTOTest3 {

    @Test
    public void testFriendWatchlistDTO() {
        // Arrange
        FriendWatchlistDTO friendWatchlistDTO = new FriendWatchlistDTO();
        Long friendId = 123L;
        String username = "testUser";
        List<String> watchlist = Arrays.asList("movie1", "movie2", "movie3");

        // Act
        friendWatchlistDTO.setFriendId(friendId);
        friendWatchlistDTO.setUsername(username);
        friendWatchlistDTO.setWatchlist(watchlist);

        // Assert
        assertEquals(friendId, friendWatchlistDTO.getFriendId());
        assertEquals(username, friendWatchlistDTO.getUsername());
        assertEquals(watchlist, friendWatchlistDTO.getWatchlist());

        //test null
        FriendWatchlistDTO emptyFriendWatchlistDTO = new FriendWatchlistDTO();
        assertNull(emptyFriendWatchlistDTO.getFriendId());
        assertNull(emptyFriendWatchlistDTO.getUsername());
        assertNull(emptyFriendWatchlistDTO.getWatchlist());
    }

    @Test
    public void testTopRatedMovieDTO() {
        // Arrange
        TopRatedMovieDTO topRatedMovieDTO = new TopRatedMovieDTO();
        String movieId = "tt1234567";
        String title = "Test Movie";
        String posterPath = "/poster.jpg";
        double rating = 8.5;
        String friendUsername = "friendUser";

        // Act
        topRatedMovieDTO.setMovieId(movieId);
        topRatedMovieDTO.setTitle(title);
        topRatedMovieDTO.setPosterPath(posterPath);
        topRatedMovieDTO.setRating(rating);
        topRatedMovieDTO.setFriendUsername(friendUsername);

        // Assert
        assertEquals(movieId, topRatedMovieDTO.getMovieId());
        assertEquals(title, topRatedMovieDTO.getTitle());
        assertEquals(posterPath, topRatedMovieDTO.getPosterPath());
        assertEquals(rating, topRatedMovieDTO.getRating(), 0.001); // Use delta for double comparison
        assertEquals(friendUsername, topRatedMovieDTO.getFriendUsername());

        //test the constructor
        TopRatedMovieDTO topRatedMovieDTO2 = new TopRatedMovieDTO(movieId, title, posterPath, rating, friendUsername);
        assertEquals(movieId, topRatedMovieDTO2.getMovieId());
        assertEquals(title, topRatedMovieDTO2.getTitle());
        assertEquals(posterPath, topRatedMovieDTO2.getPosterPath());
        assertEquals(rating, topRatedMovieDTO2.getRating(), 0.001);
        assertEquals(friendUsername, topRatedMovieDTO2.getFriendUsername());
    }
}
