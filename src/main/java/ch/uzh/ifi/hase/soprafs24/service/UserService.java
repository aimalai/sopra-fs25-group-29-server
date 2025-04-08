package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.UserSession;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs24.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * User Service - Handles authentication & session management.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    private final Set<String> tokenBlacklist = new HashSet<>();
    private final HashMap<String, OTPEntry> otpStore = new HashMap<>();

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository,
                       @Qualifier("sessionRepository") SessionRepository sessionRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.emailService = emailService;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    // UPDATED: Login now generates OTP & sends email
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

        // Generate and send OTP
        String otp = generateOTP();
        otpStore.put(username, new OTPEntry(otp, LocalDateTime.now().plusMinutes(5)));
        emailService.sendOTP(user.getEmail(), otp);
        log.info("OTP sent to user: {}", username);

        return "OTP sent to your email.";
    }

    // OTP verification method
    public String verifyOTP(String username, String otpInput) {
        OTPEntry otpEntry = otpStore.get(username);

        if (otpEntry == null || otpEntry.expirationTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired or invalid.");
        }

        if (!otpEntry.otp.equals(otpInput)) {
            throw new IllegalArgumentException("Incorrect OTP.");
        }

        otpStore.remove(username); 

        String token = generateToken(userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found.")));

        manageUserSessions(userRepository.findByUsername(username).get().getId(), token);
        return token;
    }

    private String generateOTP() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    private boolean isUserLocked(User user) {
        LocalDateTime lockoutEndTime = user.getLockoutUntil();
        return lockoutEndTime != null && LocalDateTime.now().isBefore(lockoutEndTime);
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    public String generateToken(User user) {
        return UUID.randomUUID().toString();
    }

    public void logout(String token) {
        tokenBlacklist.add(token);
    }

    public boolean isTokenValid(String token) {
        return !tokenBlacklist.contains(token);
    }

    // UPDATED: Handle first-time login (register user, generate token)
    public String handleFirstLogin(User newUser) {
        User registeredUser = registerUser(newUser);
        return generateToken(registeredUser);
    }

    // UPDATED: Register user and save email
    public User registerUser(User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        if (userRepository.existsByEmail(newUser.getEmail())) {  // NEW: Prevent duplicate emails
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken.");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setEmail(newUser.getEmail());  // NEW: Ensure email is stored

        log.info("Registering new user: {}", newUser.getUsername());
        return userRepository.saveAndFlush(newUser);
    }

    @Transactional
    public void manageUserSessions(Long userId, String token) {
        if (sessionRepository.existsByUserId(userId)) {
            sessionRepository.deleteByUserId(userId);
            log.info("Existing session deleted for user ID: {}", userId);
        }

        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setCreatedAt(LocalDateTime.now());
        sessionRepository.save(userSession);
        log.info("New session created for user ID: {}", userId);
    }

    @Transactional
    public boolean isSessionPresent(Long userId) {
        boolean sessionExists = sessionRepository.existsByUserId(userId);
        log.info("Session presence check for user ID {}: {}", userId, sessionExists);
        return sessionExists;
    }

    private static class OTPEntry {
        String otp;
        LocalDateTime expirationTime;

        OTPEntry(String otp, LocalDateTime expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }
}
