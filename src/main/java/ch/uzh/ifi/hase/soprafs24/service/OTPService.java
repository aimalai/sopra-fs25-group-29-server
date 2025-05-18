package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OTPService {

    private final Logger log = LoggerFactory.getLogger(OTPService.class);
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Map<String, OTPEntry> otpStore = new HashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    public OTPService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendOTP(String username) {
        User user = retrieveUserByUsername(username);
        String otp = generateOTP();
        otpStore.put(username, new OTPEntry(otp, LocalDateTime.now().plusMinutes(5)));
        emailService.sendOTP(user.getEmail(), otp);
        log.info("OTP sent to user with username: {}", username);
    }

    public HashMap<String, String> verifyOTP(String username, String otpInput) {
        OTPEntry entry = otpStore.get(username);
        if (entry == null || entry.expirationTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "OTP expired or invalid.");
        }
        if (!entry.otp.equals(otpInput)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Incorrect OTP provided.");
        }
        otpStore.remove(username);

        User user = retrieveUserByUsername(username);
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userService.updateUserToken(user.getId(), token);

        HashMap<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", String.valueOf(user.getId()));
        response.put("message", "OTP verified successfully");
        return response;
    }

    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private User retrieveUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User with username '" + username + "' not found.");
        }
        return user;
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
