package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

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
    checkIfUserExists(newUser);
    newUser.setCreationDate(LocalDate.now());
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
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
    if (userData.getBirthday() != null) {
      existingUser.setBirthday(userData.getBirthday());
    }
    userRepository.save(existingUser);
  }

  public void addMovieToWatchlist(Long userId, String movieId) {
    User user = getUserById(userId);
    if (user == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    if (!user.getWatchlist().contains(movieId)) {
        user.getWatchlist().add(movieId);
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

  public List<User> getUsersByUsername(String username) {
    return userRepository.findByUsernameContaining(username);
  }

}
