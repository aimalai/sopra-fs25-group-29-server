package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WatchPartyServiceTest2 {

    @Mock
    private WatchPartyRepository watchPartyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InviteRepository inviteRepository;

    @InjectMocks
    private WatchPartyService watchPartyService;

    private User organizer;
    private WatchParty watchParty;
    private Invite invite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        organizer = new User();
        organizer.setId(1L);
        organizer.setUsername("organizer");
        organizer.setEmail("organizer@example.com");

        watchParty = new WatchParty();
        watchParty.setId(10L);
        watchParty.setOrganizer(organizer);
        watchParty.setTitle("Movie Night");
        watchParty.setContentLink("example.com");
        watchParty.setDescription("Fun movie watching session");
        watchParty.setScheduledTime(LocalDateTime.now().plusDays(1));

        invite = new Invite();
        invite.setWatchPartyId(10L);
        invite.setUsername("invitedUser");
        invite.setStatus("pending");

        ReflectionTestUtils.setField(watchPartyService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void testCreateWatchParty_validInput() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        WatchParty newWatchParty = new WatchParty();
        newWatchParty.setOrganizer(organizer);
        newWatchParty.setTitle("Another Movie Night");
        newWatchParty.setContentLink("another.com");
        newWatchParty.setDescription("More movie fun");
        newWatchParty.setScheduledTime(futureTime);

        when(watchPartyRepository.save(any(WatchParty.class))).thenReturn(newWatchParty);

        WatchParty createdParty = watchPartyService.createWatchParty(organizer, "Another Movie Night", "another.com", "More movie fun", futureTime);

        assertEquals("Another Movie Night", createdParty.getTitle());
        assertEquals(organizer, createdParty.getOrganizer());
        verify(watchPartyRepository, times(1)).save(any(WatchParty.class));
    }

    @Test
    void testCreateWatchParty_invalidScheduledTime() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                watchPartyService.createWatchParty(organizer, "Past Movie Night", "past.com", "Should fail", pastTime));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Scheduled time must be in the future.", exception.getReason());
        verify(watchPartyRepository, never()).save(any(WatchParty.class));
    }

    @Test
    void testGetWatchPartiesByOrganizer() {
        when(watchPartyRepository.findByOrganizer_Id(1L)).thenReturn(List.of(watchParty));
        List<WatchParty> parties = watchPartyService.getWatchPartiesByOrganizer(1L);
        assertEquals(1, parties.size());
        assertEquals(watchParty, parties.get(0));
        verify(watchPartyRepository, times(1)).findByOrganizer_Id(1L);
    }

    @Test
    void testGetAllWatchParties() {
        when(watchPartyRepository.findAll()).thenReturn(List.of(watchParty));
        List<WatchParty> allParties = watchPartyService.getAllWatchParties();
        assertEquals(1, allParties.size());
        assertEquals(watchParty, allParties.get(0));
        verify(watchPartyRepository, times(1)).findAll();
    }

    @Test
    void testInviteUserToWatchParty_userFound_inviteSent() {
        User invitedUser = new User();
        invitedUser.setUsername("invitedUser");
        invitedUser.setEmail("invited@example.com");
        when(userRepository.findByUsername("invitedUser")).thenReturn(invitedUser);
        when(inviteRepository.save(any(Invite.class))).thenReturn(invite);

        String result = watchPartyService.inviteUserToWatchParty(10L, "invitedUser", 1L);

        assertEquals("Invite sent successfully!", result);
        verify(userRepository, times(1)).findByUsername("invitedUser");
        verify(inviteRepository, times(1)).save(any(Invite.class));
        // We cannot directly verify the private sendInviteEmail.
        // We rely on the successful saving of the invite as an indicator
        // that the invitation process was triggered.
    }

    @Test
    void testInviteUserToWatchParty_userNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

        String result = watchPartyService.inviteUserToWatchParty(10L, "nonExistentUser", 1L);

        assertEquals("Username does not exist", result);
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
        verify(inviteRepository, never()).save(any(Invite.class));
        // No direct verification of the private method call here
    }

    @Test
    void testGetInvitedUsers() {
        when(inviteRepository.findByWatchPartyId(10L)).thenReturn(List.of(invite));
        List<String> invited = watchPartyService.getInvitedUsers(10L);
        assertEquals(1, invited.size());
        assertEquals("invitedUser", invited.get(0));
        verify(inviteRepository, times(1)).findByWatchPartyId(10L);
    }

    @Test
    void testUpdateInviteStatus_inviteFound() {
        when(inviteRepository.findByWatchPartyIdAndUsername(10L, "invitedUser")).thenReturn(List.of(invite));
        when(inviteRepository.save(any(Invite.class))).thenReturn(invite);

        boolean updated = watchPartyService.updateInviteStatus(10L, "invitedUser", "accepted");

        assertTrue(updated);
        assertEquals("accepted", invite.getStatus());
        verify(inviteRepository, times(1)).findByWatchPartyIdAndUsername(10L, "invitedUser");
        verify(inviteRepository, times(1)).save(invite);
    }

    @Test
    void testUpdateInviteStatus_inviteNotFound() {
        when(inviteRepository.findByWatchPartyIdAndUsername(10L, "nonInvitedUser")).thenReturn(Collections.emptyList());

        boolean updated = watchPartyService.updateInviteStatus(10L, "nonInvitedUser", "declined");

        assertFalse(updated);
        verify(inviteRepository, times(1)).findByWatchPartyIdAndUsername(10L, "nonInvitedUser");
        verify(inviteRepository, never()).save(any(Invite.class));
    }

    @Test
    void testGetLatestInviteResponses() {
        Invite inviteAccepted = new Invite();
        inviteAccepted.setUsername("user1");
        inviteAccepted.setStatus("accepted");
        Invite invitePending = new Invite();
        invitePending.setUsername("user2");
        invitePending.setStatus("pending");
        when(inviteRepository.findByWatchPartyId(10L)).thenReturn(List.of(inviteAccepted, invitePending));

        List<String> responses = watchPartyService.getLatestInviteResponses(10L);
        assertEquals(2, responses.size());
        assertTrue(responses.contains("user1 - accepted"));
        assertTrue(responses.contains("user2 - pending"));
        verify(inviteRepository, times(1)).findByWatchPartyId(10L);
    }

    @Test
    void testGetWatchPartyById_found() {
        when(watchPartyRepository.findById(10L)).thenReturn(Optional.of(watchParty));
        WatchParty foundParty = watchPartyService.getWatchPartyById(10L);
        assertEquals(watchParty, foundParty);
        verify(watchPartyRepository, times(1)).findById(10L);
    }

    @Test
    void testGetWatchPartyById_notFound() {
        when(watchPartyRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> watchPartyService.getWatchPartyById(99L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Watch-party not found.", exception.getReason());
        verify(watchPartyRepository, times(1)).findById(99L);
    }

    @Test
    void testGetWatchPartiesForInvitee_acceptedInvitesFound() {
        Invite acceptedInvite1 = new Invite();
        acceptedInvite1.setWatchPartyId(10L);
        acceptedInvite1.setStatus("accepted");
        Invite acceptedInvite2 = new Invite();
        acceptedInvite2.setWatchPartyId(11L);
        acceptedInvite2.setStatus("accepted");
        WatchParty party1 = new WatchParty();
        party1.setId(10L);
        WatchParty party2 = new WatchParty();
        party2.setId(11L);

        when(inviteRepository.findByUsernameAndStatus("invitedUser", "accepted")).thenReturn(List.of(acceptedInvite1, acceptedInvite2));
        when(watchPartyRepository.findById(10L)).thenReturn(Optional.of(party1));
        when(watchPartyRepository.findById(11L)).thenReturn(Optional.of(party2));

        List<WatchParty> parties = watchPartyService.getWatchPartiesForInvitee("invitedUser");
        assertEquals(2, parties.size());
        assertTrue(parties.contains(party1));
        assertTrue(parties.contains(party2));
        verify(inviteRepository, times(1)).findByUsernameAndStatus("invitedUser", "accepted");
        verify(watchPartyRepository, times(1)).findById(10L);
        verify(watchPartyRepository, times(1)).findById(11L);
    }

    @Test
    void testGetWatchPartiesForInvitee_noAcceptedInvites() {
        when(inviteRepository.findByUsernameAndStatus("invitedUser", "accepted")).thenReturn(Collections.emptyList());
        List<WatchParty> parties = watchPartyService.getWatchPartiesForInvitee("invitedUser");
        assertTrue(parties.isEmpty());
        verify(inviteRepository, times(1)).findByUsernameAndStatus("invitedUser", "accepted");
        verify(watchPartyRepository, never()).findById(anyLong());
    }

    @Test
    void testGetWatchPartiesForInvitee_acceptedInvite_partyNotFound() {
        Invite acceptedInvite = new Invite();
        acceptedInvite.setWatchPartyId(10L);
        acceptedInvite.setStatus("accepted");

        when(inviteRepository.findByUsernameAndStatus("invitedUser", "accepted")).thenReturn(List.of(acceptedInvite));
        when(watchPartyRepository.findById(10L)).thenReturn(Optional.empty());

        List<WatchParty> parties = watchPartyService.getWatchPartiesForInvitee("invitedUser");
        assertTrue(parties.isEmpty());
        verify(inviteRepository, times(1)).findByUsernameAndStatus("invitedUser", "accepted");
        verify(watchPartyRepository, times(1)).findById(10L);
    }
}