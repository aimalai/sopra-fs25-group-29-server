package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerIntegrationTest3 {

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Test: Valid friend request
    @Test
    public void sendFriendRequest_validInput_success() throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("fromUserId", "1");

        mockMvc.perform(post("/users/2/friendrequests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Friend request sent"));
    }

    // Test: Missing fromUserId in friend request
    @Test
    public void sendFriendRequest_missingFromUserId_error() throws Exception {
        mockMvc.perform(post("/users/2/friendrequests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fromUserId is required"));
    }

    // Test: Accept friend request
    @Test
    public void acceptFriendRequest_validInput_success() throws Exception {
        mockMvc.perform(put("/users/2/friendrequests/1/accept")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Friend request accepted"));
    }

    // Test: Decline friend request
    @Test
    public void declineFriendRequest_validInput_success() throws Exception {
        mockMvc.perform(delete("/users/2/friendrequests/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Test: Get user watchlist
    @Test
    public void getWatchlist_validUser_success() throws Exception {
        List<String> watchlist = Collections.singletonList("{\"movieId\":\"1\",\"title\":\"Movie Title\"}");
        given(userService.getWatchlist(2L)).willReturn(watchlist);

        mockMvc.perform(get("/users/2/watchlist")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("{\"movieId\":\"1\",\"title\":\"Movie Title\"}"));
    }

    // Test: Add to watchlist
    @Test
    public void addToWatchlist_validInput_success() throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("movieId", "1");
        requestBody.put("title", "Movie Title");

        mockMvc.perform(post("/users/2/watchlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Movie added to watchlist"));
    }

    // Test: Remove from watchlist
    @Test
    public void removeFromWatchlist_validInput_success() throws Exception {
        mockMvc.perform(delete("/users/2/watchlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
