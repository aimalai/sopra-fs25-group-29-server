package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.TopRatedMovieDTO;
import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserRatingController {

    private final UserRatingService userRatingService;

    @Autowired
    public UserRatingController(UserRatingService userRatingService) {
        this.userRatingService = userRatingService;
    }

    @GetMapping("/users/{userId}/friends/top-rated")
    public ResponseEntity<List<TopRatedMovieDTO>> getTopRatedByFriends(
            @PathVariable String userId,
            @RequestParam(defaultValue = "4") double minRating
    ) {
        List<TopRatedMovieDTO> topRated = userRatingService.getTopRatedByFriends(userId, minRating);
        return ResponseEntity.ok(topRated);
    }

    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<?> getUserRating(
            @PathVariable String userId,
            @RequestParam String movieId
    ) {
        return userRatingService.getUserRating(userId, movieId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{userId}/ratings")
    public ResponseEntity<UserRating> createOrUpdateUserRating(
            @PathVariable String userId,
            @RequestBody UserRatingRequest request
    ) {
        UserRating userRating = userRatingService.createOrUpdateUserRating(
                userId,
                request.getUsername(),
                request.getMovieId(),
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.ok(userRating);
    }

    @GetMapping("/movies/{movieId}/userRatings")
    public ResponseEntity<?> getAggregatedUserRating(
            @PathVariable String movieId
    ) {
        double avgRating = userRatingService.getAverageRatingForMovie(movieId);
        long totalRatings = userRatingService.getTotalRatingsForMovie(movieId);
        return ResponseEntity.ok(new AggregatedRating(avgRating, totalRatings));
    }

    @GetMapping("/movies/{movieId}/ratings")
    public ResponseEntity<List<UserRating>> getAllRatings(@PathVariable String movieId) {
        return ResponseEntity.ok(userRatingService.getRatingsForMovie(movieId));
    }

    public static class UserRatingRequest {
        private String movieId;
        private double rating;
        private String comment;
        private String username;

        public String getMovieId() { return movieId; }
        public void setMovieId(String movieId) { this.movieId = movieId; }
        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class AggregatedRating {
        private double averageRating;
        private long totalRatings;

        public AggregatedRating(double averageRating, long totalRatings) {
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
        }

        public double getAverageRating() { return averageRating; }
        public long getTotalRatings() { return totalRatings; }
    }
}
