package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import org.hibernate.annotations.DynamicUpdate; // Import DynamicUpdate annotation
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Internal User Representation
 * This class defines how the user is stored in the database.
 */
@Entity
@DynamicUpdate // Ensures Hibernate only updates modified fields
@Table(name = "users") // Updated to match the database table name
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password; // Added password for secure storage

  @Column(unique = true)
  private String email; // Optional email for future use

  @Column
  private String profilePicture; // Optional profile picture URL

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now(); // Automatically set on creation

  @Column
  private LocalDateTime updatedAt; // For tracking updates

  @Column
  private LocalDateTime lastLoginTime; // Tracks the last login time

  @Column(nullable = false)
  private int failedLoginAttempts = 0; // Tracks the number of failed login attempts

  @Column
  private LocalDateTime lockoutUntil; // Tracks the timestamp until which the user is locked out

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
