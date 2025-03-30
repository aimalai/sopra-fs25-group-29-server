package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGetDTO {

  private Long id;
  private String password;
  private String username;
  private UserStatus status;
  private LocalDate creationDate;
  private String birthday;
  private String token;

  public String getBirthday() {
    return birthday;
  }
  public void setBirthday(String birthday) {
    this.birthday = birthday;
  }

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }

  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }
  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }
  public void setCreationDate(LocalDate creationDate) {
    this.creationDate = creationDate;
  }

  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
  }
}
