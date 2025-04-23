package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
    @Autowired ObjectMapper mapper;

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

    @Test
    void createWatchParty_valid_ok() throws Exception {
        Map<String,String> payload = Map.of(
            "organizerId","1",
            "title","Birthday",
            "contentLink","http://link",
            "description","desc",
            "scheduledTime","2025-04-30T12:00:00"
        );
        String json = mapper.writeValueAsString(payload);
        User organizer = new User(); organizer.setId(1L);
        WatchParty wp = new WatchParty();
        wp.setOrganizer(organizer);
        wp.setTitle("Birthday");
        when(userService.getUserById(1L)).thenReturn(organizer);
        when(watchPartyService.createWatchParty(eq(organizer), eq("Birthday"), eq("http://link"), eq("desc"), eq(LocalDateTime.parse("2025-04-30T12:00:00"))))
            .thenReturn(wp);
        mvc.perform(post("/api/watchparties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.organizer.id").value(1))
           .andExpect(jsonPath("$.title").value("Birthday"));
    }

    @Test
    void createWatchParty_invalidDate_badRequest() throws Exception {
        Map<String,String> payload = Map.of(
            "organizerId","1",
            "title","Title",
            "contentLink","link",
            "description","desc",
            "scheduledTime","bad-date"
        );
        String json = mapper.writeValueAsString(payload);
        mvc.perform(post("/api/watchparties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
           .andExpect(status().isBadRequest());
    }

    @Test
    void createWatchParty_organizerNotFound_notFound() throws Exception {
        Map<String,String> payload = Map.of(
            "organizerId","2",
            "title","Party",
            "contentLink","link",
            "description","desc",
            "scheduledTime","2025-04-30T12:00:00"
        );
        String json = mapper.writeValueAsString(payload);
        when(userService.getUserById(2L)).thenReturn(null);
        mvc.perform(post("/api/watchparties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
           .andExpect(status().isNotFound());
    }

    @Test
    void inviteUser_success_ok() throws Exception {
        when(watchPartyService.inviteUserToWatchParty(1L,"alice",2L))
            .thenReturn("Invitation sent");
        mvc.perform(post("/api/watchparties/1/invites")
                .param("username","alice")
                .param("inviterId","2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.message").value("Invitation sent"));
    }

    @Test
    void inviteUser_userNotFound_notFound() throws Exception {
        when(watchPartyService.inviteUserToWatchParty(1L,"bob",2L))
            .thenReturn("Username does not exist");
        mvc.perform(post("/api/watchparties/1/invites")
                .param("username","bob")
                .param("inviterId","2"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Username does not exist"));
    }

    @Test
    void getInvitedUsers_ok() throws Exception {
        when(watchPartyService.getInvitedUsers(3L))
            .thenReturn(List.of("u1","u2"));
        mvc.perform(get("/api/watchparties/3/invites"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("u1"));
    }

    @Test
    void handleInviteResponse_success_ok() throws Exception {
        when(watchPartyService.updateInviteStatus(5L,"user","ACCEPTED"))
            .thenReturn(true);
        mvc.perform(get("/api/watchparties/5/invite-response")
                .param("username","user")
                .param("status","ACCEPTED"))
           .andExpect(status().isOk())
           .andExpect(content().string("Invite response recorded successfully!"));
    }

    @Test
    void handleInviteResponse_failure_notFound() throws Exception {
        when(watchPartyService.updateInviteStatus(5L,"user","DECLINED"))
            .thenReturn(false);
        mvc.perform(get("/api/watchparties/5/invite-response")
                .param("username","user")
                .param("status","DECLINED"))
           .andExpect(status().isNotFound())
           .andExpect(content().string("Failed to update invite response."));
    }

    @Test
    void getLatestInviteResponses_ok() throws Exception {
        when(watchPartyService.getLatestInviteResponses(6L))
            .thenReturn(List.of("x - OK"));
        mvc.perform(get("/api/watchparties/6/latest-invite-status"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0]").value("x - OK"));
    }
}
