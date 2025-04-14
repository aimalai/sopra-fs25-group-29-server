package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.service.UserRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserRatingController {

    private final UserRatingService userRatingService;

    @Autowired
    public UserRatingController(UserRatingService userRatingService) {
        this.userRatingService = userRatingService;
    }

    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<?> getUserRating(
            @PathVariable String userId,
            @RequestParam String movieId
    ) {
        Optional<UserRating> userRatingOptional = userRatingService.getUserRating(userId, movieId);
        if (userRatingOptional.isPresent()) {
            return ResponseEntity.ok(userRatingOptional.get());
        } else {
            return ResponseEntity.status(404).body("Rating not found for user: " + userId + " and movie: " + movieId);
        }
    }

    @PostMapping("/users/{userId}/ratings")
    public ResponseEntity<UserRating> createOrUpdateUserRating(
            @PathVariable String userId,
            @RequestBody UserRatingRequest request
    ) {
        UserRating userRating = userRatingService.createOrUpdateUserRating(userId, request.getMovieId(), request.getRating());
        return ResponseEntity.ok(userRating);
    }

    @GetMapping("/movies/{movieId}/userRatings")
    public ResponseEntity<Map<String, Object>> getAggregatedUserRating(
            @PathVariable String movieId
    ) {
        double avgRating = userRatingService.getAverageRatingForMovie(movieId);
        long totalRatings = userRatingService.getTotalRatingsForMovie(movieId);
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", avgRating);
        response.put("totalRatings", totalRatings);
        return ResponseEntity.ok(response);
    }

    public static class UserRatingRequest {
        private String movieId;
        private double rating;

        public String getMovieId() {
            return movieId;
        }

        public void setMovieId(String movieId) {
            this.movieId = movieId;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }
    }
}