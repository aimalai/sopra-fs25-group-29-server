package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.WatchPartyService;

@WebMvcTest(WatchPartyController.class)
class WatchPartyControllerRestTest {

    @Autowired MockMvc mvc;
    @MockBean WatchPartyService watchPartyService;
    @MockBean UserService userService;

    @Test
    void getAllParties_ok() throws Exception {
        when(watchPartyService.getAllWatchParties()).thenReturn(List.of());
        mvc.perform(get("/api/watchparties"))
           .andExpect(status().isOk());
    }

    @Test
    void getWatchPartiesByOrganizer_ok() throws Exception {
        WatchParty wp = new WatchParty();
        User u = new User(); u.setId(1L);
        wp.setOrganizer(u);
        when(watchPartyService.getWatchPartiesByOrganizer(1L))
            .thenReturn(List.of(wp));

        mvc.perform(get("/api/watchparties")
                 .param("organizerId","1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].organizer.id").value(1));
    }
}
