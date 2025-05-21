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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerIntegrationTest2 {

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

    // Test: Valid User Creation
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

    // Test: Invalid Username for OTP Verification
    @Test
    public void verifyOTP_missingUsername_error() throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("otp", "123456"); // No username provided

        mockMvc.perform(post("/users/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is required for OTP verification."));
    }

    // Test: Retrieve Friends of a User
    @Test
    public void getFriends_validUser_returnsFriends() throws Exception {
        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friendUsername");
        friend.setEmail("friend@example.com");

        List<User> friends = Collections.singletonList(friend);
        given(userService.getFriends(1L)).willReturn(friends);

        mockMvc.perform(get("/users/1/friends")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(friend.getId().intValue())))
                .andExpect(jsonPath("$[0].username", is(friend.getUsername())))
                .andExpect(jsonPath("$[0].email", is(friend.getEmail())));
    }

    // Test: Friend Request Missing Payload
    @Test
    public void sendFriendRequest_missingPayload_error() throws Exception {
        mockMvc.perform(post("/users/1/friendrequests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Empty payload
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fromUserId is required"));
    }

    // Test: Invalid User Logout
    @Test
    public void logoutUser_invalidUser_throwsException() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).logoutUser(999L);

        mockMvc.perform(put("/users/999/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
