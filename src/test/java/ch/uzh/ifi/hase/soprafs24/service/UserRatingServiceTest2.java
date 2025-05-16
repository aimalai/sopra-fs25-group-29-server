package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.repository.UserRatingRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TopRatedMovieDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRatingServiceTest2 {

    @Mock
    private UserRatingRepository userRatingRepository;

    @Mock
    private UserService userService;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private UserRatingService userRatingService;

    private UserRating userRating1;
    private UserRating userRating2;
    private ch.uzh.ifi.hase.soprafs24.entity.User friend1;
    private ch.uzh.ifi.hase.soprafs24.entity.User friend2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userRating1 = new UserRating("1", "user1", "movie1", 4.5, "Great movie!");
        userRating2 = new UserRating("2", "user2", "movie1", 3.0, "Okay.");

        friend1 = new ch.uzh.ifi.hase.soprafs24.entity.User();
        friend1.setId(2L);
        friend1.setUsername("friend1");
        friend2 = new ch.uzh.ifi.hase.soprafs24.entity.User();
        friend2.setId(3L);
        friend2.setUsername("friend2");
    }

    @Test
    void testGetTopRatedByFriends_noFriends() {
        when(userService.getFriends(1L)).thenReturn(Collections.emptyList());
        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends("1", 3.0);
        assertTrue(topRated.isEmpty());
    }

    @Test
    void testGetTopRatedByFriends_noRatingsFromFriends() {
        when(userService.getFriends(1L)).thenReturn(List.of(friend1, friend2));
        when(userRatingRepository.findByUserIdInAndRatingGreaterThanEqual(List.of("2", "3"), 3.0))
                .thenReturn(Collections.emptyList());
        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends("1", 3.0);
        assertTrue(topRated.isEmpty());
    }

    @Test
    void testGetTopRatedByFriends_ratedByFriends_notOwnRated_notOnWatchlist() throws IOException {
        when(userService.getFriends(1L)).thenReturn(List.of(friend1));
        when(userRatingRepository.findByUserIdInAndRatingGreaterThanEqual(List.of("2"), 4.0))
                .thenReturn(List.of(userRating1));
        when(userRatingRepository.findByUserId("1")).thenReturn(Collections.emptyList());
        when(userService.getWatchlist(1L)).thenReturn(Collections.emptyList());
        when(movieService.getMediaDetails("movie1", "movie"))
                .thenReturn("{\"title\": \"Movie Title\", \"poster_path\": \"/path.jpg\"}");

        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends("1", 4.0);
        assertEquals(1, topRated.size());
        assertEquals("movie1", topRated.get(0).getMovieId());
        assertEquals("Movie Title", topRated.get(0).getTitle());
        assertEquals("/path.jpg", topRated.get(0).getPosterPath());
        assertEquals(4.5, topRated.get(0).getRating());
        assertEquals("user1", topRated.get(0).getFriendUsername());
    }

    @Test
    void testGetTopRatedByFriends_ratedByFriends_ownRated() {
        when(userService.getFriends(1L)).thenReturn(List.of(friend1));
        when(userRatingRepository.findByUserIdInAndRatingGreaterThanEqual(List.of("2"), 4.0))
                .thenReturn(List.of(userRating1));
        when(userRatingRepository.findByUserId("1")).thenReturn(List.of(new UserRating("1", "me", "movie1", 5.0, "")));
        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends("1", 4.0);
        assertTrue(topRated.isEmpty());
    }

    @Test
    void testGetTopRatedByFriends_ratedByFriends_onWatchlist() {
        when(userService.getFriends(1L)).thenReturn(List.of(friend1));
        when(userRatingRepository.findByUserIdInAndRatingGreaterThanEqual(List.of("2"), 4.0))
                .thenReturn(List.of(userRating1));
        when(userRatingRepository.findByUserId("1")).thenReturn(Collections.emptyList());
        when(userService.getWatchlist(1L)).thenReturn(List.of("{\"movieId\": \"movie1\"}"));
        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends("1", 4.0);
        assertTrue(topRated.isEmpty());
    }

    @Test
    void testGetUserRating() {
        when(userRatingRepository.findByUserIdAndMovieId("1", "movie1")).thenReturn(Optional.of(userRating1));
        Optional<UserRating> rating = userRatingService.getUserRating("1", "movie1");
        assertTrue(rating.isPresent());
        assertEquals(userRating1, rating.get());
    }

    @Test
    void testGetUserRating_notExists() {
        when(userRatingRepository.findByUserIdAndMovieId("1", "movie1")).thenReturn(Optional.empty());
        Optional<UserRating> rating = userRatingService.getUserRating("1", "movie1");
        assertFalse(rating.isPresent());
    }

    @Test
    void testCreateOrUpdateUserRating_create() {
        when(userRatingRepository.findByUserIdAndMovieId("3", "movieX")).thenReturn(Optional.empty());
        UserRating newUserRating = new UserRating("3", "user3", "movieX", 5.0, "Amazing!");
        when(userRatingRepository.save(any(UserRating.class))).thenReturn(newUserRating);

        UserRating result = userRatingService.createOrUpdateUserRating("3", "user3", "movieX", 5.0, "Amazing!");
        assertEquals(newUserRating, result);
        verify(userRatingRepository, times(1)).save(any(UserRating.class));
    }


    @Test
    void testGetAverageRatingForMovie_noRatings() {
        when(userRatingRepository.findByMovieId("movieX")).thenReturn(Collections.emptyList());
        assertEquals(0.0, userRatingService.getAverageRatingForMovie("movieX"));
    }

    @Test
    void testGetAverageRatingForMovie_withRatings() {
        when(userRatingRepository.findByMovieId("movie1")).thenReturn(List.of(userRating1, userRating2));
        assertEquals(3.75, userRatingService.getAverageRatingForMovie("movie1"), 0.001);
    }

    @Test
    void testGetTotalRatingsForMovie() {
        when(userRatingRepository.findByMovieId("movie1")).thenReturn(List.of(userRating1, userRating2));
        assertEquals(2, userRatingService.getTotalRatingsForMovie("movie1"));
    }

    @Test
    void testGetRatingsForMovie() {
        when(userRatingRepository.findByMovieId("movie1")).thenReturn(List.of(userRating1, userRating2));
        List<UserRating> ratings = userRatingService.getRatingsForMovie("movie1");
        assertEquals(2, ratings.size());
        assertTrue(ratings.contains(userRating1));
        assertTrue(ratings.contains(userRating2));
    }
}
