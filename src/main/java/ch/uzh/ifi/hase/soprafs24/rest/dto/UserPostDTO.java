package ch.uzh.ifi.hase.soprafs24.rest.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserPostDTO {

    @NotNull(message = "Username is required")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password; // Added password for registration

    private String email; // Optional email for future scalability

    @NotNull(message = "Confirm Password is required")
    @NotBlank(message = "Confirm Password cannot be blank")
    private String confirmPassword; // Added confirmPassword for validation

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
