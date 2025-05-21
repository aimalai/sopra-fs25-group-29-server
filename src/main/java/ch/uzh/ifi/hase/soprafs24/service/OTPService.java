package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class OTPService {

    private final Logger log = LoggerFactory.getLogger(OTPService.class);
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final HashMap<String, OTPEntry> otpStore = new HashMap<>();

    @Autowired
    public OTPService(@Qualifier("userRepository") UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Send OTP to user
    public void sendOTP(String username) {
        User user = retrieveUserByUsername(username); 
        String otp = generateOTP();
        otpStore.put(username, new OTPEntry(otp, LocalDateTime.now().plusMinutes(5)));

        emailService.sendOTP(user.getEmail(), otp);
        log.info("OTP sent to user with username: {}", username);
    }

    // Verify the OTP provided by the user
    public HashMap<String, String> verifyOTP(String username, String otpInput) {
        OTPEntry otpEntry = otpStore.get(username);

        if (otpEntry == null || otpEntry.expirationTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "OTP expired or invalid."
            );
        }

        if (!otpEntry.otp.equals(otpInput)) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Incorrect OTP provided."
            );
        }

        otpStore.remove(username);

        User user = retrieveUserByUsername(username); // Retrieve user for userId
        log.info("OTP verified successfully for username: {}", username);

        HashMap<String, String> response = new HashMap<>();
        response.put("message", "OTP verified successfully");
        response.put("token", "OTP verified successfully for username: " + username);
        response.put("userId", String.valueOf(user.getId())); 

        return response;
    }

    // Generate a 6-digit OTP
    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // Helper method to retrieve User by username
    private User retrieveUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "User with username '" + username + "' not found."
            );
        }
        return user;
    }

    // store OTP and expiration time
    private static class OTPEntry {
        String otp;
        LocalDateTime expirationTime;

        OTPEntry(String otp, LocalDateTime expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }
}
