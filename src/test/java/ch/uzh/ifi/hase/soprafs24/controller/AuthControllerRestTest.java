package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserUpdateDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class AuthControllerRestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OTPService otpService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void getCurrentUser_me_ok() throws Exception {
        User mockUser = new User();
        mockUser.setId(42L);
        mockUser.setUsername("alice");
        mockUser.setEmail("alice@example.com");
        mockUser.setStatus(UserStatus.ONLINE);
        when(userService.getUserByToken("valid-token")).thenReturn(mockUser);

        mvc.perform(get("/users/me")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }

    @Test
    void updateUser_profile_ok() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setUsername("bob");
        dto.setEmail("bob@example.com");
        doNothing().when(userService).updateUser(eq(1L), any(User.class));

        User updated = new User();
        updated.setId(1L);
        updated.setUsername("bob");
        updated.setEmail("bob@example.com");
        updated.setStatus(UserStatus.ONLINE);
        when(userService.getUserById(1L)).thenReturn(updated);

        mvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }
}
