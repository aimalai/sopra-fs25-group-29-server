package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Internal User Representation
 * This class defines how the user is stored in the database.
 */
@Entity
@DynamicUpdate
@Table(name = "users")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true) // NEW: Email is required
  private String email;

  @Column
  private String profilePicture;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column
  private LocalDateTime updatedAt;

  @Column
  private LocalDateTime lastLoginTime;

  @Column(nullable = false)
  private int failedLoginAttempts = 0;

  @Column
  private LocalDateTime lockoutUntil;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(LocalDateTime lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public int getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  public void setFailedLoginAttempts(int failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public LocalDateTime getLockoutUntil() {
    return lockoutUntil;
  }

  public void setLockoutUntil(LocalDateTime lockoutUntil) {
    this.lockoutUntil = lockoutUntil;
  }
}
