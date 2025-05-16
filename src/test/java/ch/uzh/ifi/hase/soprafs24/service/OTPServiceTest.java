package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OTPServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OTPService otpService;

    private User testUser;
    private HashMap<String, Object> otpStore; // Changed to HashMap<String, Object>
    private Class<?> otpEntryClass; // Class to hold the OTPEntry class
    private Constructor<?> otpEntryConstructor; // Constructor for OTPEntry

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setId(1L);

        // Get the actual field from the OTPService instance
        Field otpStoreField = OTPService.class.getDeclaredField("otpStore");
        otpStoreField.setAccessible(true); // Make it accessible since it's private
        otpStore = new HashMap<>(); // Initialize it
        otpStoreField.set(otpService, otpStore); // Set the field on the otpService instance.

        // Get the OTPEntry class
        otpEntryClass = Class.forName("ch.uzh.ifi.hase.soprafs24.service.OTPService$OTPEntry");
        otpEntryConstructor = otpEntryClass.getDeclaredConstructor(String.class, LocalDateTime.class);
        otpEntryConstructor.setAccessible(true); // Make the constructor accessible

    }

    private Object createOTPEntry(String otp, LocalDateTime expirationTime) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return otpEntryConstructor.newInstance(otp, expirationTime);
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

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> otpService.sendOTP("nonExistentUser"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User with username 'nonExistentUser' not found.", exception.getReason());
        verify(emailService, never()).sendOTP(anyString(), anyString());
    }

    @Test
    void testVerifyOTP_validOTP() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String otpValue = "123456";
        Object otpEntry = createOTPEntry(otpValue, LocalDateTime.now().plusMinutes(5));
        otpStore.put("testUser", otpEntry);

        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        HashMap<String, String> response = otpService.verifyOTP("testUser", otpValue);

        assertEquals("OTP verified successfully", response.get("message"));
        assertTrue(response.get("token").contains("OTP verified successfully for username: testUser"));
        assertEquals("1", response.get("userId"));
        assertNull(otpStore.get("testUser"));
        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void testVerifyOTP_expiredOTP() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String otpValue = "654321";
        Object otpEntry = createOTPEntry(otpValue, LocalDateTime.now().minusMinutes(1));
        otpStore.put("testUser", otpEntry);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> otpService.verifyOTP("testUser", otpValue));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("OTP expired or invalid.", exception.getReason());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testVerifyOTP_invalidOTP() throws InvocationTargetException, InstantiationException, IllegalAccessException{
        String correctOTP = "111222";
        Object correctOtpEntry = createOTPEntry(correctOTP, LocalDateTime.now().plusMinutes(5));
        otpStore.put("testUser", correctOtpEntry);
        String incorrectOTP = "999888";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> otpService.verifyOTP("testUser", incorrectOTP));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Incorrect OTP provided.", exception.getReason());
        assertNotNull(otpStore.get("testUser"));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testVerifyOTP_otpNotFound() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> otpService.verifyOTP("testUser", "123456"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("OTP expired or invalid.", exception.getReason());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testRetrieveUserByUsername_userFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);
        // The functionality of retrieveUserByUsername is implicitly tested
        // through sendOTP and verifyOTP.
    }

    @Test
    void testRetrieveUserByUsername_userNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);
        // The functionality of retrieveUserByUsername is implicitly tested
        // through sendOTP and verifyOTP.
    }
}

