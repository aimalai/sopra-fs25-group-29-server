package ch.uzh.ifi.hase.soprafs24.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.CustomResponseException;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FriendWatchlistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final OTPService otpService;
    private final Map<String, FailedLoginData> loginFailures = new HashMap<>();

    UserController(UserService userService, OTPService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PostMapping("/{userId}/upload-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String imageUrl = userService.uploadProfilePicture(userId, file);
            Map<String, String> response = new HashMap<>();
            response.put("profilePictureUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed.");
        }
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody UserPostDTO userPostDTO) {
        String username = userPostDTO.getUsername();
        String password = userPostDTO.getPassword();

        try {
            if (isUserLocked(username)) {
                throw new CustomResponseException(
                        "Too many failed attempts. Your account is locked. Please try again later.",
                        HttpStatus.FORBIDDEN
                );
            }

            User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
            User loggedInUser = userService.loginUser(userInput.getUsername(), userInput.getPassword());

            resetFailedLogins(username);
            otpService.sendOTP(loggedInUser.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP sent to your registered email.");
            response.put("userId", loggedInUser.getId());

            return ResponseEntity.ok(response);

        } catch (CustomResponseException e) {
            handleFailedLogin(username);
            throw e;
        } catch (IllegalArgumentException e) {
            handleFailedLogin(username);
            throw new CustomResponseException("Login failed: invalid username or password.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            handleFailedLogin(username);
            throw new CustomResponseException("An unexpected error occurred. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleFailedLogin(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return;
        }
        FailedLoginData data = loginFailures.getOrDefault(username, new FailedLoginData(0, null));
        if (data.getLockoutUntil() != null && LocalDateTime.now().isBefore(data.getLockoutUntil())) {
            return;
        }
        int attempts = data.getAttempts() + 1;
        if (attempts >= 5) {
            data.setLockoutUntil(LocalDateTime.now().plusMinutes(30));
        }
        data.setAttempts(attempts);
        loginFailures.put(username, data);
    }

    private boolean isUserLocked(String username) {
        FailedLoginData data = loginFailures.get(username);
        return data != null
                && data.getLockoutUntil() != null
                && LocalDateTime.now().isBefore(data.getLockoutUntil());
    }

    private void resetFailedLogins(String username) {
        loginFailures.remove(username);
    }

    static class FailedLoginData {
        private int attempts;
        private LocalDateTime lockoutUntil;

        public FailedLoginData(int attempts, LocalDateTime lockoutUntil) {
            this.attempts = attempts;
            this.lockoutUntil = lockoutUntil;
        }

        public int getAttempts() {
            return attempts;
        }

        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        public LocalDateTime getLockoutUntil() {
            return lockoutUntil;
        }

        public void setLockoutUntil(LocalDateTime lockoutUntil) {
            this.lockoutUntil = lockoutUntil;
        }
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserGetDTO getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found.");
        }
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PutMapping("/{userId}/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutUser(@PathVariable Long userId) {
        userService.logoutUser(userId);
    }

    @PostMapping("/{userId}/watchlist")
    @ResponseStatus(HttpStatus.CREATED)
    public String addToWatchlist(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload
    ) {
        String movieId = payload.get("movieId");
        String mediaType = payload.getOrDefault("mediaType", "movie");
        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "movieId is required");
        }
        String addedOn = LocalDateTime.now().toString();
        String json = String.format(
                "{\"movieId\":\"%s\",\"title\":\"%s\",\"posterPath\":\"%s\",\"mediaType\":\"%s\",\"addedOn\":\"%s\"}",
                movieId,
                payload.getOrDefault("title", ""),
                payload.getOrDefault("posterPath", ""),
                mediaType,
                addedOn
        );
        userService.addMovieToWatchlist(userId, json);
        return "{\"message\": \"Movie added to watchlist\"}";
    }

    @GetMapping("/{userId}/watchlist")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getWatchlist(@PathVariable Long userId) {
        return userService.getWatchlist(userId);
    }

    @DeleteMapping("/{userId}/watchlist/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWatchlist(
            @PathVariable Long userId,
            @PathVariable String movieId
    ) {
        userService.removeMovieFromWatchlist(userId, movieId);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<UserGetDTO> getAllUsers(@RequestParam(value = "username", required = false) String username) {
        List<User> users = (username != null && !username.trim().isEmpty())
                ? userService.getUsersByUsername(username)
                : userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("/{userId}/friendrequests")
    @ResponseStatus(HttpStatus.CREATED)
    public String sendFriendRequest(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload
    ) {
        String fromUserIdStr = payload.get("fromUserId");
        if (fromUserIdStr == null || fromUserIdStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromUserId is required");
        }
        userService.sendFriendRequest(userId, Long.parseLong(fromUserIdStr));
        return "{\"message\": \"Friend request sent\"}";
    }

    @PutMapping("/{userId}/friendrequests/{fromUserId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public String acceptFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long fromUserId
    ) {
        userService.acceptFriendRequest(userId, fromUserId);
        return "{\"message\": \"Friend request accepted\"}";
    }

    @DeleteMapping("/{userId}/friendrequests/{fromUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long fromUserId
    ) {
        userService.declineFriendRequest(userId, fromUserId);
    }

    @GetMapping("/{userId}/friendrequests")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getFriendRequests(@PathVariable Long userId) {
        return userService.getFriendRequests(userId);
    }

    @GetMapping("/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserGetDTO> getFriends(@PathVariable Long userId) {
        List<User> friends = userService.getFriends(userId);
        List<UserGetDTO> friendDTOs = new ArrayList<>();
        for (User friend : friends) {
            friendDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(friend));
        }
        return friendDTOs;
    }

    @GetMapping("/{userId}/friends/watchlists")
    @ResponseStatus(HttpStatus.OK)
    public List<FriendWatchlistDTO> getFriendsWatchlists(@PathVariable Long userId) {
        List<User> friends = userService.getFriends(userId);
        List<FriendWatchlistDTO> result = new ArrayList<>();
        for (User friend : friends) {
            if (Boolean.TRUE.equals(friend.isSharable())) {
                FriendWatchlistDTO dto = new FriendWatchlistDTO();
                dto.setFriendId(friend.getId());
                dto.setUsername(friend.getUsername());
                dto.setWatchlist(friend.getWatchlist());
                result.add(dto);
            }
        }
        return result;
    }

    @PostMapping("/otp/send")
    @ResponseStatus(HttpStatus.OK)
    public void sendOTP(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required to send OTP.");
        }
        otpService.sendOTP(username);
    }

    @PostMapping("/otp/verify")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> verifyOTP(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String otp = payload.get("otp");
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required for OTP verification.");
        }
        if (otp == null || otp.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required for verification.");
        }
        try {
            Map<String, String> response = otpService.verifyOTP(username, otp);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("error", "An unexpected error occurred during OTP verification."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserGetDTO getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replaceFirst("^Bearer\\s+", "");
        User current = userService.getUserByToken(token);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(current);
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserGetDTO updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        User userData = DTOMapper.INSTANCE.convertUserUpdateDTOtoEntity(userUpdateDTO);
        userService.updateUser(userId, userData);
        User updated = userService.getUserById(userId);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updated);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(
        @PathVariable Long userId,
        @PathVariable Long friendId
    ) {
        userService.removeFriend(userId, friendId);
    }

}