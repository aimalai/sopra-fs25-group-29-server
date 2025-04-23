package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.entity.UserRating;
import ch.uzh.ifi.hase.soprafs24.service.UserRatingService;

@WebMvcTest(UserRatingController.class)
class UserRatingControllerRestTest {

    @Autowired MockMvc mvc;
    @MockBean UserRatingService service;
    @Autowired ObjectMapper mapper;

    @Test
    void getUserRating_found() throws Exception {
        UserRating r = new UserRating("u1","bob","m1",4.0,"ok");
        when(service.getUserRating("u1","m1")).thenReturn(Optional.of(r));
        mvc.perform(get("/api/users/u1/ratings").param("movieId","m1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.movieId").value("m1"));
    }

    @Test
    void getUserRating_notFound() throws Exception {
        when(service.getUserRating("u1","m1")).thenReturn(Optional.empty());
        mvc.perform(get("/api/users/u1/ratings").param("movieId","m1"))
           .andExpect(status().isNotFound());
    }

    @Test
    void getAggregatedUserRating_ok() throws Exception {
        when(service.getAverageRatingForMovie("m1")).thenReturn(3.5);
        when(service.getTotalRatingsForMovie("m1")).thenReturn(2L);
        mvc.perform(get("/api/movies/m1/userRatings"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.averageRating").value(3.5))
           .andExpect(jsonPath("$.totalRatings").value(2));
    }

    @Test
    void getAllRatings_ok() throws Exception {
        when(service.getRatingsForMovie("m1")).thenReturn(List.of(new UserRating("u1","bob","m1",4.0,"ok")));
        mvc.perform(get("/api/movies/m1/ratings"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].username").value("bob"));
    }
}
