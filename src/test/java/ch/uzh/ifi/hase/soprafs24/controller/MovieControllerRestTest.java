package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs24.service.MovieService;

@WebMvcTest(MovieController.class)
class MovieControllerRestTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MovieService movieService;

    @Test
    void searchCombined_ok() throws Exception {
        when(movieService.searchCombined("q", null, false, 1, 5)).thenReturn("[]");
        mvc.perform(get("/api/movies/search").param("query", "q"))
           .andExpect(status().isOk())
           .andExpect(content().string("[]"));
    }

    @Test
    void getDetails_ok() throws Exception {
        when(movieService.getMediaDetails("5", "movie")).thenReturn("{\"id\":5}");
        mvc.perform(get("/api/movies/details")
                .param("id", "5").param("media_type", "movie"))
           .andExpect(status().isOk())
           .andExpect(content().string("{\"id\":5}"));
    }

    @Test
    void getTrending_returnsOkAndJsonBody() throws Exception {
        String trendingJson = "{ \"results\": [ { \"id\": 123, \"title\": \"Test Movie\" } ] }";
        when(movieService.getTrending()).thenReturn(trendingJson);

        mvc.perform(get("/api/movies/trending"))
           .andExpect(status().isOk())
           .andExpect(content().json(trendingJson));

        verify(movieService).getTrending();
    }
}