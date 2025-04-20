package ch.uzh.ifi.hase.soprafs24.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.utils.ImageUploadUtil;
import jakarta.mail.MessagingException;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmailService emailService;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);

        if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        checkIfUserExists(newUser);
        newUser.setCreationDate(LocalDate.now());
        newUser = userRepository.save(newUser);
        userRepository.flush();

        try {
            emailService.sendEmail(
                    newUser.getEmail(),
                    "Welcome to Flicks & Friends!",
                    "Thank you for registering. We're excited to have you on board!"
            );
            log.info("Welcome email sent to {}", newUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", newUser.getEmail(), e.getMessage());
        }

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String filename = "user_" + userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String imageUrl = ImageUploadUtil.saveImage(file, filename);

        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not exist.");
        }
        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password.");
        }
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);
        return user;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void logoutUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(UserStatus.OFFLINE);
        userRepository.save(user);
    }

    public void updateUser(Long userId, User userData) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (userData.getUsername() != null && !userData.getUsername().isEmpty()) {
            User userByUsername = userRepository.findByUsername(userData.getUsername());
            if (userByUsername != null && !userByUsername.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
            }
            existingUser.setUsername(userData.getUsername());
        }

        if (userData.getPassword() != null) {
            existingUser.setPassword(userData.getPassword());
        }

        if (userData.getBirthday() != null) {
            existingUser.setBirthday(userData.getBirthday());
        }

        if (userData.getEmail() != null) {
            User userByEmail = userRepository.findByEmail(userData.getEmail());
            if (userByEmail != null && !userByEmail.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use.");
            }
            existingUser.setEmail(userData.getEmail());
        }

        if (userData.getBiography() != null) {
            existingUser.setBiography(userData.getBiography());
        }

        if (userData.getProfilePictureUrl() != null) {
            existingUser.setProfilePictureUrl(userData.getProfilePictureUrl());
        }

        if (userData.isSharable() != existingUser.isSharable()) {
            existingUser.setSharable(userData.isSharable());
        }

        if (userData.isPublicRatings() != existingUser.isPublicRatings()) {
            existingUser.setPublicRatings(userData.isPublicRatings());
        }

        userRepository.save(existingUser);
    }

    public void addMovieToWatchlist(Long userId, String jsonString) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!user.getWatchlist().contains(jsonString)) {
            user.getWatchlist().add(jsonString);
            userRepository.save(user);
        }
    }

    public List<String> getWatchlist(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user.getWatchlist();
    }

    public void removeMovieFromWatchlist(Long userId, String movieId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        List<String> watchlist = user.getWatchlist();
        watchlist.removeIf(entry -> {
            try {
                Map<?, ?> parsed = objectMapper.readValue(entry, Map.class);
                return movieId.equals(parsed.get("movieId"));
            } catch (Exception e) {
                return false;
            }
        });

        userRepository.save(user);
    }

    public List<User> getUsersByUsername(String username) {
        return userRepository.findByUsernameContaining(username);
    }

    public void sendFriendRequest(Long targetUserId, Long fromUserId) {
        User targetUser = getUserById(targetUserId);
        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }
        if (targetUser.getIncomingFriendRequests().contains(fromUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend request already sent");
        }
        if (targetUser.getFriends().contains(fromUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Users are already friends");
        }
        targetUser.getIncomingFriendRequests().add(fromUserId);
        userRepository.save(targetUser);
    }

    public void acceptFriendRequest(Long targetUserId, Long fromUserId) {
        User targetUser = getUserById(targetUserId);
        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }
        if (!targetUser.getIncomingFriendRequests().contains(fromUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No friend request from this user");
        }
        targetUser.getIncomingFriendRequests().remove(fromUserId);
        if (!targetUser.getFriends().contains(fromUserId)) {
            targetUser.getFriends().add(fromUserId);
        }
        userRepository.save(targetUser);

        User sender = getUserById(fromUserId);
        if (sender != null && !sender.getFriends().contains(targetUserId)) {
            sender.getFriends().add(targetUserId);
            userRepository.save(sender);
        }
    }

    public void declineFriendRequest(Long targetUserId, Long fromUserId) {
        User targetUser = getUserById(targetUserId);
        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }
        if (targetUser.getIncomingFriendRequests().contains(fromUserId)) {
            targetUser.getIncomingFriendRequests().remove(fromUserId);
            userRepository.save(targetUser);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No friend request from this user");
        }
    }

    public List<Long> getFriendRequests(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return new ArrayList<>(user.getIncomingFriendRequests());
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<User> friendsList = new ArrayList<>();
        for (Long friendId : user.getFriends()) {
            User friend = getUserById(friendId);
            if (friend != null) {
                friendsList.add(friend);
            }
        }
        return friendsList;
    }

    public boolean areFriends(Long userId, Long otherUserId) {
        User user = getUserById(userId);
        return user != null && user.getFriends().contains(otherUserId);
    }
}
