package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs24.service.MovieService;

@WebMvcTest(MovieController.class)
class MovieControllerRestTest {

    @Autowired MockMvc mvc;
    @MockBean MovieService movieService;

    @Test
    void searchCombined_ok() throws Exception {
        when(movieService.searchCombined("q", null, false, 1, 5)).thenReturn("[]");
        mvc.perform(get("/api/movies/search").param("query","q"))
           .andExpect(status().isOk())
           .andExpect(content().string("[]"));
    }

    @Test
    void getDetails_ok() throws Exception {
        when(movieService.getMediaDetails("5","movie")).thenReturn("{\"id\":5}");
        mvc.perform(get("/api/movies/details")
                .param("id","5").param("media_type","movie"))
           .andExpect(status().isOk())
           .andExpect(content().string("{\"id\":5}"));
    }
}
