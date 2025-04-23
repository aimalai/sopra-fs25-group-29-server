package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    List<User> findByUsernameContaining(String username);

    User findByEmail(String email);

    User findByToken(String token);
}
