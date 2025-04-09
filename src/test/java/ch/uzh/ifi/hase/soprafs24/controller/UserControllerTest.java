package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // User attributes
        User user = new User();
        user.setId(1L);
        user.setPassword("User.1234");
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        // Input Username and Password
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("User.1234");
        userPostDTO.setUsername("testUsername");

        // create the mock object
        given(userService.createUser(Mockito.any())).willReturn(user);

        // vergliech
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.password", is(user.getPassword())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_usernameExists_conflict() throws Exception {
        // create a user
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("User.1234");
        userPostDTO.setUsername("existingUser");
        // mock the error
        Mockito.doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"))
                .when(userService).createUser(Mockito.any());
        // test if error
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void getUserById_success() throws Exception {
        // create
        User user = new User();
        user.setId(1L);
        user.setPassword("User.1234");
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);

        // get user
        given(userService.getUserById(1L)).willReturn(user);

        // compare
        mockMvc.perform(get("/users/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.password", is(user.getPassword())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    public void getUserById_notFound() throws Exception {
        // set the mock to return null
        given(userService.getUserById(99L)).willReturn(null);

        // try to get user
        mockMvc.perform(get("/users/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_success() throws Exception {
        // sim success of updating user
        Mockito.doNothing().when(userService).updateUser(Mockito.eq(1L), Mockito.any());

        // changes
        String updateBody = "{\"username\": \"updatedUsername\", \"birthday\": \"2000-01-01\"}";

        // put
        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateUser_notFound() throws Exception {
        // fake the error  
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).updateUser(Mockito.eq(99L), Mockito.any());

        // set user
        String updateBody = "{\"username\": \"whatever\"}";

        // try to put
        mockMvc.perform(put("/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created. %s", e.toString()));
        }
    }
}
