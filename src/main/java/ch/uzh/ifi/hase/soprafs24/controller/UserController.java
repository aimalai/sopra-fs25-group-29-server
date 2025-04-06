package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
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
    @ResponseBody
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
    public void updateUser(@PathVariable Long userId, @RequestBody User userData) {
        userService.updateUser(userId, userData);
    }

    @PostMapping("/{userId}/watchlist")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String addToWatchlist(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String movieId = payload.get("movieId");
        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "movieId is required");
        }
        userService.addMovieToWatchlist(userId, movieId);
        return "{\"message\": \"Movie added to watchlist\"}";
    }

    @GetMapping("/{userId}/watchlist")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<String> getWatchlist(@PathVariable Long userId) {
        return userService.getWatchlist(userId);
    }
}
