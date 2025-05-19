package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OTPServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OTPService otpService;

    private User testUser;
    private HashMap<String, Object> otpStore;
    private Constructor<?> otpEntryConstructor;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setId(1L);

        Field otpStoreField = OTPService.class.getDeclaredField("otpStore");
        otpStoreField.setAccessible(true);
        otpStore = new HashMap<>();
        otpStoreField.set(otpService, otpStore);

        Field userServiceField = OTPService.class.getDeclaredField("userService");
        userServiceField.setAccessible(true);
        userServiceField.set(otpService, userService);

        Class<?> otpEntryClass = Class.forName("ch.uzh.ifi.hase.soprafs24.service.OTPService$OTPEntry");
        otpEntryConstructor = otpEntryClass.getDeclaredConstructor(String.class, LocalDateTime.class);
        otpEntryConstructor.setAccessible(true);
    }

    private Object createOTPEntry(String otp, LocalDateTime expiration) throws Exception {
        return otpEntryConstructor.newInstance(otp, expiration);
    }

    @Test
    void testSendOTP_userFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        doNothing().when(emailService).sendOTP(eq("test@example.com"), anyString());

        otpService.sendOTP("testUser");

        verify(userRepository, times(1)).findByUsername("testUser");
        verify(emailService, times(1)).sendOTP(eq("test@example.com"), anyString());
    }

    @Test
    void testSendOTP_userNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> otpService.sendOTP("nonExistentUser"));

        assertEquals("User with username 'nonExistentUser' not found.", exception.getReason());
        verify(emailService, never()).sendOTP(anyString(), anyString());
    }

    @Test
    void testVerifyOTP_validOTP() throws Exception {
        String otpValue = "123456";
        otpStore.put("testUser", createOTPEntry(otpValue, LocalDateTime.now().plusMinutes(5)));
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        doNothing().when(userService).updateUserToken(eq(1L), anyString());

        HashMap<String, String> response = otpService.verifyOTP("testUser", otpValue);

        assertEquals("OTP verified successfully", response.get("message"));
        assertNotNull(response.get("token"));
        assertEquals("1", response.get("userId"));
        assertNull(otpStore.get("testUser"));
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(userService, times(1)).updateUserToken(eq(1L), anyString());
    }

    @Test
    void testVerifyOTP_expiredOTP() throws Exception {
        String otpValue = "654321";
        otpStore.put("testUser", createOTPEntry(otpValue, LocalDateTime.now().minusMinutes(1)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> otpService.verifyOTP("testUser", otpValue));

        assertEquals("OTP expired or invalid.", exception.getReason());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testVerifyOTP_invalidOTP() throws Exception {
        String correctOTP = "111222";
        otpStore.put("testUser", createOTPEntry(correctOTP, LocalDateTime.now().plusMinutes(5)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> otpService.verifyOTP("testUser", "999888"));

        assertEquals("Incorrect OTP provided.", exception.getReason());
        assertNotNull(otpStore.get("testUser"));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testVerifyOTP_otpNotFound() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> otpService.verifyOTP("testUser", "123456"));

        assertEquals("OTP expired or invalid.", exception.getReason());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testRetrieveUserByUsername_userFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        otpService.sendOTP("testUser");
    }

    @Test
    void testRetrieveUserByUsername_userNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);
        assertThrows(ResponseStatusException.class,
            () -> otpService.sendOTP("nonExistentUser"));
    }
}
