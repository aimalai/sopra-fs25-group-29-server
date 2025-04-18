package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedInUser = userService.loginUser(userInput.getUsername(), userInput.getPassword());
        String token = java.util.UUID.randomUUID().toString();
        loggedInUser.setToken(token);
        userService.updateUser(loggedInUser.getId(), loggedInUser);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
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

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
        User userData = DTOMapper.INSTANCE.convertUserUpdateDTOtoEntity(userUpdateDTO);
        userService.updateUser(userId, userData);
    }

    @PostMapping("/{userId}/watchlist")
    @ResponseStatus(HttpStatus.CREATED)
    public String addToWatchlist(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String movieId = payload.get("movieId");
        String title = payload.get("title");
        String posterPath = payload.get("posterPath");

        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "movieId is required");
        }

        String addedOn = LocalDateTime.now().toString();
        String json = String.format(
            "{\"movieId\":\"%s\",\"title\":\"%s\",\"posterPath\":\"%s\",\"addedOn\":\"%s\"}",
            movieId,
            title != null ? title : "",
            posterPath != null ? posterPath : "",
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
    public void removeFromWatchlist(@PathVariable Long userId, @PathVariable String movieId) {
        userService.removeMovieFromWatchlist(userId, movieId);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<UserGetDTO> getAllUsers(@RequestParam(value = "username", required = false) String username) {
        List<User> users;
        if (username != null && !username.trim().isEmpty()) {
            users = userService.getUsersByUsername(username);
        } else {
            users = userService.getUsers();
        }
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("/{userId}/friendrequests")
    @ResponseStatus(HttpStatus.CREATED)
    public String sendFriendRequest(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String fromUserIdStr = payload.get("fromUserId");
        if (fromUserIdStr == null || fromUserIdStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromUserId is required");
        }
        Long fromUserId = Long.parseLong(fromUserIdStr);
        userService.sendFriendRequest(userId, fromUserId);
        return "{\"message\": \"Friend request sent\"}";
    }

    @PutMapping("/{userId}/friendrequests/{fromUserId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public String acceptFriendRequest(@PathVariable Long userId, @PathVariable Long fromUserId) {
        userService.acceptFriendRequest(userId, fromUserId);
        return "{\"message\": \"Friend request accepted\"}";
    }

    @DeleteMapping("/{userId}/friendrequests/{fromUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineFriendRequest(@PathVariable Long userId, @PathVariable Long fromUserId) {
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
}
