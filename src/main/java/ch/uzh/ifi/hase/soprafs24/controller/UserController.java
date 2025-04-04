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
            // Validate that confirm password matches password
            if (!userPostDTO.getPassword().equals(userPostDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match!");
            }

            User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

            // Automatically handle first login after registration
            String token = userService.handleFirstLogin(userInput);
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
            // Attempt login with the userService
            String token = userService.attemptLogin(userPostDTO.getUsername(), userPostDTO.getPassword());
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Handle lockout or invalid login with clear feedback
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Account locked")) {
                return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN); // Lockout-specific status
            }
            return new ResponseEntity<>(errorMessage, HttpStatus.UNAUTHORIZED); // Invalid credentials
        } catch (Exception e) {
            // Generic error handling
            return new ResponseEntity<>("An error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
