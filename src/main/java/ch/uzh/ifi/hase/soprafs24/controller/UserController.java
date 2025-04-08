package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * Handles all REST requests related to users.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserGetDTO> getAllUsers() {
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserPostDTO userPostDTO) {
        try {
            if (!userPostDTO.getPassword().equals(userPostDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match!");
            }

            User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
            String token = userService.handleFirstLogin(userInput);

            // Add a new session for the newly registered user using user ID
            userService.manageUserSessions(userInput.getId(), token);

            // Ensure session exists before responding
            if (!userService.isSessionPresent(userInput.getId())) {
                throw new IllegalArgumentException("Session creation failed.");
            }

            return new ResponseEntity<>(token, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserPostDTO userPostDTO) {
        try {
            // UPDATED: Login now sends OTP instead of returning token
            String responseMessage = userService.attemptLogin(userPostDTO.getUsername(), userPostDTO.getPassword());
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Account locked")) {
                return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(errorMessage, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // NEW: OTP Verification Endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody UserPostDTO userPostDTO) {
        try {
            String token = userService.verifyOTP(userPostDTO.getUsername(), userPostDTO.getOtp()); // Using new OTP method
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("OTP verification failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Missing token.");
            }
            userService.logout(token);
            return new ResponseEntity<>("Logout successful.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during logout.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
