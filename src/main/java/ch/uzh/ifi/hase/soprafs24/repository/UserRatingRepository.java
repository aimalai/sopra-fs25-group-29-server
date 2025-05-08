package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    Optional<UserRating> findByUserIdAndMovieId(String userId, String movieId);
    List<UserRating> findByMovieId(String movieId);
    List<UserRating> findByUserIdInAndRatingGreaterThanEqual(List<String> userIds, double rating);
    List<UserRating> findByUserId(String userId);
}
