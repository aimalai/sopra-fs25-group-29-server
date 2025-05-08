package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.repository.UserRatingRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TopRatedMovieDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class UserRatingService {

    private final UserRatingRepository userRatingRepository;
    private final UserService userService;
    private final MovieService movieService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserRatingService(UserRatingRepository userRatingRepository,
                             UserService userService,
                             MovieService movieService) {
        this.userRatingRepository = userRatingRepository;
        this.userService = userService;
        this.movieService = movieService;
    }

    public List<TopRatedMovieDTO> getTopRatedByFriends(String userId, double minRating) {
        long uid = Long.parseLong(userId);
        List<ch.uzh.ifi.hase.soprafs24.entity.User> friends = userService.getFriends(uid);
        List<String> friendIds = friends.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.toList());
        
        List<UserRating> friendRatings = userRatingRepository
                .findByUserIdInAndRatingGreaterThanEqual(friendIds, minRating);

        Set<String> ownRatedMovieIds = userRatingRepository.findByUserId(userId).stream()
                .map(UserRating::getMovieId)
                .collect(Collectors.toSet());

        Set<String> ownWatchlistMovieIds = userService.getWatchlist(uid).stream()
                .map(json -> {
                    try {
                        JsonNode node = objectMapper.readTree(json);
                        return node.path("movieId").asText();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        return friendRatings.stream()
                .filter(r -> !ownRatedMovieIds.contains(r.getMovieId()))
                .filter(r -> !ownWatchlistMovieIds.contains(r.getMovieId()))
                .map(r -> {
                    try {
                        String details = movieService.getMediaDetails(r.getMovieId(), "movie");
                        JsonNode node = objectMapper.readTree(details);
                        String title = node.has("title") ? node.get("title").asText() : node.get("name").asText();
                        String posterPath = node.has("poster_path") ? node.get("poster_path").asText() : "";
                        return new TopRatedMovieDTO(
                                r.getMovieId(),
                                title,
                                posterPath,
                                r.getRating(),
                                r.getUsername()
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("Error parsing movie details for ID " + r.getMovieId(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    public Optional<UserRating> getUserRating(String userId, String movieId) {
        return userRatingRepository.findByUserIdAndMovieId(userId, movieId);
    }

    public UserRating createOrUpdateUserRating(String userId, String username, String movieId,
                                               double rating, String comment) {
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
        if (ratings.isEmpty()) return 0.0;
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
