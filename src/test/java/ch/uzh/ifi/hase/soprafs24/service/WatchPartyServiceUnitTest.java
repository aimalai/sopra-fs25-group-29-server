package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;

@ExtendWith(MockitoExtension.class)
class WatchPartyServiceUnitTest {

    @Mock private WatchPartyRepository wpRepo;
    @Mock private InviteRepository inviteRepo;

    @InjectMocks private WatchPartyService service;

    private User organizer = new User();

    @BeforeEach
    void setUp() {
        organizer.setId(1L);
    }

    @Test
    void createWatchParty_savesWhenFutureTime() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(wpRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        WatchParty wp = service.createWatchParty(organizer, "link1", "desc", null, future);
        assertEquals(organizer, wp.getOrganizer());
        verify(wpRepo).save(any(WatchParty.class));
    }

    @Test
    void createWatchParty_pastTime_throws400() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        assertThrows(ResponseStatusException.class, () ->
            service.createWatchParty(organizer, "link1", "desc", null, past)
        );
    }

    @Test
    void getInvitedUsers_and_latestInviteResponses() {
        Invite inv = new Invite(); inv.setWatchPartyId(2L); inv.setUsername("x"); inv.setStatus("OK");
        when(inviteRepo.findByWatchPartyId(2L)).thenReturn(List.of(inv));
        List<String> users = service.getInvitedUsers(2L);
        assertEquals(List.of("x"), users);
        List<String> latest = service.getLatestInviteResponses(2L);
        assertEquals(List.of("x - OK"), latest);
    }

    @Test
    void getWatchPartyById_found() {
        WatchParty wp = new WatchParty();
        wp.setId(5L);
        when(wpRepo.findById(5L)).thenReturn(Optional.of(wp));
        WatchParty result = service.getWatchPartyById(5L);
        assertEquals(5L, result.getId());
    }

    @Test
    void getWatchPartyById_notFound_throws404() {
        when(wpRepo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.getWatchPartyById(7L));
    }
}
