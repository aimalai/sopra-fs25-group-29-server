package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setPassword("User.1234");
        user.setUsername("firstname@lastname");
        user.setEmail("test@example.com"); // Added email field
        user.setStatus(UserStatus.ONLINE);
        user.setToken("1");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertNotNull(found.getId());
        assertEquals(found.getPassword(), user.getPassword());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getEmail(), user.getEmail()); // Verify email field
        assertEquals(found.getToken(), user.getToken());
        assertEquals(found.getStatus(), user.getStatus());
    }
}
