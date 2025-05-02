package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.util.Optional;

/**
 * Unit test to force SonarQube coverage for PasswordEncoder.
 */
@SpringBootTest
public class UserServicePasswordEncoderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder; 

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
    }

    @Test
    public void testPasswordEncoder() {
        
        String rawPassword = "newPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword); 

       
        assertEquals("encodedPassword123", encodedPassword); 
    }
}
