package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<UserSession, Long> {

    // Finds a UserSession by user ID
    Optional<UserSession> findByUserId(Long userId);

    // Checks if a session exists for the specified user ID
    boolean existsByUserId(Long userId);

    @Transactional
    // Deletes all sessions for a given user ID
    void deleteByUserId(Long userId);
}
