package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.repository.UserRatingRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserRatingServiceUnitTest {

    @Mock
    private UserRatingRepository repo;

    @InjectMocks
    private UserRatingService service;

    private UserRating existing;

    @BeforeEach
    void setUp() {
        existing = new UserRating("u1","bob","m1",4.0,"nice");

        lenient().when(repo.findByUserIdAndMovieId("u1","m1"))
                 .thenReturn(Optional.of(existing));

        lenient().when(repo.save(existing)).thenReturn(existing);
    }

    @Test
    void createOrUpdateUserRating_updatesExisting() {
        UserRating updated = service.createOrUpdateUserRating("u1","bob","m1",5.0,"great");
        assertEquals(5.0, updated.getRating());
        assertEquals("great", updated.getComment());
        verify(repo).save(existing);
    }

    @Test
    void createOrUpdateUserRating_createsNew() {
        when(repo.findByUserIdAndMovieId("u2","m2"))
            .thenReturn(Optional.empty());
        when(repo.save(any(UserRating.class)))
            .thenAnswer(i -> i.getArgument(0));

        UserRating created = service.createOrUpdateUserRating("u2","alice","m2",3.0,"ok");
        assertEquals("m2", created.getMovieId());
        assertEquals("alice", created.getUsername());
        verify(repo).save(any(UserRating.class));
    }

    @Test
    void getAverageRatingForMovie_handlesEmpty() {
        when(repo.findByMovieId("mx")).thenReturn(List.of());
        assertEquals(0.0, service.getAverageRatingForMovie("mx"));
    }

    @Test
    void getTotalRatingsForMovie_counts() {
        when(repo.findByMovieId("m1")).thenReturn(List.of(existing, existing));
        assertEquals(2, service.getTotalRatingsForMovie("m1"));
    }

    @Test
    void getRatingsForMovie_returnsList() {
        when(repo.findByMovieId("m1")).thenReturn(List.of(existing));
        List<UserRating> list = service.getRatingsForMovie("m1");
        assertEquals(1, list.size());
    }
}
