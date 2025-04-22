package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerInegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OTPService otpService;

    @Autowired
    private ObjectMapper objectMapper;

    private String asJsonString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created. %s", e.toString()));
        }
    }

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setPassword("User.1234");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("User.1234");
        userPostDTO.setUsername("testUsername");
        userPostDTO.setEmail("test@example.com");

        given(userService.createUser(Mockito.any())).willReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void verifyOTP_validOTP_success() throws Exception {
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "OTP verified successfully");
        response.put("token", "testToken");
        response.put("userId", "1");

        given(otpService.verifyOTP(Mockito.anyString(), Mockito.anyString())).willReturn(response);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "testUsername");
        requestBody.put("otp", "123456");

        mockMvc.perform(post("/users/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(response.get("message"))))
                .andExpect(jsonPath("$.token", is(response.get("token"))))
                .andExpect(jsonPath("$.userId", is(response.get("userId"))));
    }

    @Test
    public void verifyOTP_invalidOTP_error() throws Exception {
        Mockito.doThrow(new RuntimeException("An unexpected error occurred during OTP verification."))
                .when(otpService).verifyOTP(Mockito.anyString(), Mockito.anyString());

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "testUsername");
        requestBody.put("otp", "000000");

        mockMvc.perform(post("/users/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("An unexpected error occurred during OTP verification.")));
    }
}
