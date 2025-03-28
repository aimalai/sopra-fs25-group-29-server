package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * Responsible for functionality related to the user (e.g., creating, modifying, login).
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // Password hashing

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User registerUser(User newUser) {
        // Validate mandatory fields
        if (newUser.getUsername() == null || newUser.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // Validate password strength
        if (!isValidPassword(newUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long and include letters, numbers, and special characters.");
        }

        // Hash the password
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // Set creation metadata
        newUser.setCreatedAt(java.time.LocalDateTime.now());

        // Save the new user
        User registeredUser = userRepository.saveAndFlush(newUser);
        log.debug("Registered User: {}", registeredUser);

        return registeredUser;
    }

    public String handleFirstLogin(User newUser) {
        // Register the user
        User registeredUser = this.registerUser(newUser);

        // Generate and return the token for first login
        return this.generateToken(registeredUser);
    }

    public User login(String username, String password) {
        // Validate mandatory fields
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }

        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));

        // Check if the provided password matches the stored hashed password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        log.debug("User logged in: {}", user.getUsername());
        return user;
    }

    public String generateToken(User user) {
        // Generate a simple UUID token for the user (can be enhanced further with JWT in production)
        String token = UUID.randomUUID().toString();
        log.debug("Generated token for user: {}", user.getUsername());
        return token;
    }

    private boolean isValidPassword(String password) {
        // Validate password strength: minimum 8 characters, at least one letter, one number, and one special character
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordPattern);
    }
}
