package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.repository.UserRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRatingService {

    private final UserRatingRepository userRatingRepository;

    @Autowired
    public UserRatingService(UserRatingRepository userRatingRepository) {
        this.userRatingRepository = userRatingRepository;
    }

    public Optional<UserRating> getUserRating(String userId, String movieId) {
        return userRatingRepository.findByUserIdAndMovieId(userId, movieId);
    }

    public UserRating createOrUpdateUserRating(String userId, String username, String movieId, double rating, String comment) {
        Optional<UserRating> existingRating = userRatingRepository.findByUserIdAndMovieId(userId, movieId);
        if (existingRating.isPresent()) {
            UserRating userRating = existingRating.get();
            userRating.setRating(rating);
            userRating.setComment(comment);
            userRating.setUsername(username);
            return userRatingRepository.save(userRating);
        } else {
            UserRating newRating = new UserRating(userId, username, movieId, rating, comment);
            return userRatingRepository.save(newRating);
        }
    }

    public double getAverageRatingForMovie(String movieId) {
        List<UserRating> ratings = userRatingRepository.findByMovieId(movieId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToDouble(UserRating::getRating).sum();
        return sum / ratings.size();
    }

    public long getTotalRatingsForMovie(String movieId) {
        return userRatingRepository.findByMovieId(movieId).size();
    }

    public List<UserRating> getRatingsForMovie(String movieId) {
        return userRatingRepository.findByMovieId(movieId);
    }
}
