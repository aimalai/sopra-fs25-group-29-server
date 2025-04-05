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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Service
 * Responsible for functionality related to the user (e.g., creating, modifying, login).
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // Password hashing

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    // Token blacklist for invalidated tokens
    private final Set<String> tokenBlacklist = new HashSet<>();

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public String attemptLogin(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("Invalid username or password."));

        if (isUserLocked(user)) {
            throw new IllegalArgumentException("Account locked due to too many failed login attempts. Try again after: " + user.getLockoutUntil() + ".");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedLogin(user);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = generateToken(user);
        log.info("User logged in successfully: {}", username);
        return token;
    }

    private boolean isUserLocked(User user) {
        LocalDateTime lockoutEndTime = user.getLockoutUntil();
        if (lockoutEndTime != null && LocalDateTime.now().isBefore(lockoutEndTime)) {
            log.warn("User {} is locked out until {}", user.getUsername(), lockoutEndTime);
            return true;
        }
        return false;
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        log.warn("Failed login attempt for user: {}, attempts: {}", user.getUsername(), user.getFailedLoginAttempts());

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("User {} locked out until {}", user.getUsername(), user.getLockoutUntil());
        }
        userRepository.save(user);
    }

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        log.debug("Generated token for user: {}", user.getUsername());
        return token;
    }

    public void logout(String token) {
        // Invalidate the token by adding it to the blacklist
        tokenBlacklist.add(token);
        log.info("Token invalidated: {}", token);
    }

    public boolean isTokenValid(String token) {
        // Check if the token is blacklisted
        return !tokenBlacklist.contains(token);
    }

    public String handleFirstLogin(User newUser) {
        User registeredUser = this.registerUser(newUser);
        return this.generateToken(registeredUser);
    }

    public User registerUser(User newUser) {
        if (newUser.getUsername() == null || newUser.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        if (!isValidPassword(newUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must meet strength requirements.");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());

        log.info("Registering new user: {}", newUser.getUsername());
        return userRepository.saveAndFlush(newUser);
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordPattern);
    }
}
