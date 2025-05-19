package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerRestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OTPService otpService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void getUserById_found() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");
        when(userService.getUserById(1L)).thenReturn(u);

        mvc.perform(get("/users/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.getUserById(2L)).thenReturn(null);
        mvc.perform(get("/users/2"))
           .andExpect(status().isNotFound());
    }

    @Test
    void searchUsers_withUsernameParam_returnsUsers() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setUsername("alice");
        when(userService.getUsersByUsername("ali")).thenReturn(List.of(u));

        mvc.perform(get("/users").param("username", "ali"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void getFriendRequests_validUser_returnsList() throws Exception {
        when(userService.getFriendRequests(7L)).thenReturn(List.of(2L, 3L));

        mvc.perform(get("/users/7/friendrequests"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value(2))
           .andExpect(jsonPath("$[1]").value(3));
    }

    @Test
    void getFriendRequests_userNotFound_returns404() throws Exception {
        when(userService.getFriendRequests(8L))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mvc.perform(get("/users/8/friendrequests"))
           .andExpect(status().isNotFound());
    }
}
