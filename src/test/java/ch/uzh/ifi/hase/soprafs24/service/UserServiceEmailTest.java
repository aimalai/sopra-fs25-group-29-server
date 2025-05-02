package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; 
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

/**
 * Unit tests for email-related logic in UserService.
 */
@SpringBootTest
public class UserServiceEmailTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Autowired 
    private UserService userService;

    @BeforeEach
    public void setup() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void createUser_duplicateEmail_throwsException() {
        User existingUser = new User();
        existingUser.setUsername("testUser");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("SecurePass123");

        
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(existingUser);

        User newUser = new User();
        newUser.setUsername("testUser2");
        newUser.setEmail("duplicate@example.com");
        newUser.setPassword("SecurePass123");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(newUser));
    }

    @Test
    public void createUser_sendsWelcomeEmail_success() throws MessagingException {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("SecurePass123");

        doNothing().when(emailService).sendEmail(eq("test@example.com"), eq("Welcome to Flicks & Friends!"), anyString());

        userService.createUser(newUser);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), eq("Welcome to Flicks & Friends!"), anyString());
    }

    @Test
    public void createUser_emailSendingFails_logsError() throws MessagingException {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("SecurePass123");

        doThrow(new MessagingException("Email service unavailable"))
                .when(emailService).sendEmail(eq("test@example.com"), eq("Welcome to Flicks & Friends!"), anyString());

        userService.createUser(newUser);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), eq("Welcome to Flicks & Friends!"), anyString());
    }
}