package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.OTPService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerRestTest {

    @Autowired MockMvc mvc;
    @MockBean UserService userService;
    @MockBean OTPService otpService;

    @Test
    void getUserById_found() throws Exception {
        User u = new User(); u.setId(1L); u.setUsername("bob");
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
}
