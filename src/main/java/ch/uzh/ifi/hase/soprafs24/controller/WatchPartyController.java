package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchparties")
public class WatchPartyController {

    private final WatchPartyService watchPartyService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WatchPartyController(
            WatchPartyService watchPartyService,
            UserService userService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.watchPartyService = watchPartyService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public WatchParty createWatchParty(@RequestBody Map<String, String> payload) {
        try {
            Long organizerId = Long.parseLong(payload.get("organizerId"));
            String title = payload.get("title");
            String contentLink = payload.get("contentLink");
            String description = payload.get("description");
            String scheduledTimeStr = payload.get("scheduledTime");
            LocalDateTime scheduledTime = LocalDateTime.parse(scheduledTimeStr);
            User organizer = userService.getUserById(organizerId);
            if (organizer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer not found.");
            }
            return watchPartyService.createWatchParty(organizer, title, contentLink, description, scheduledTime);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload: " + e.getMessage());
        }
    }

    @PostMapping("/{watchPartyId}/invites")
    public ResponseEntity<Map<String, String>> inviteUser(
            @PathVariable Long watchPartyId,
            @RequestParam String username,
            @RequestParam Long inviterId) {

        String responseMessage = watchPartyService.inviteUserToWatchParty(watchPartyId, username, inviterId);
        if (!"Username does not exist".equals(responseMessage)) {
            User invited = userService.findByUsername(username);
            User inviter = userService.getUserById(inviterId);
            if (invited != null) {
                NotificationDTO dto = new NotificationDTO(
                    "watchParty",
                    inviter.getUsername() +
                      " invited you to a Watchparty. Please check your Mailbox!",
                    String.valueOf(watchPartyId)
                );
                messagingTemplate.convertAndSend(
                    "/topic/notifications." + invited.getId(),
                    dto
                );
            }
        }
        Map<String, String> response = Map.of("message", responseMessage);
        return "Username does not exist".equals(responseMessage)
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
                : ResponseEntity.ok(response);
    }

    @GetMapping("/{watchPartyId}/invites")
    public ResponseEntity<List<String>> getInvitedUsers(@PathVariable Long watchPartyId) {
        List<String> invitedUsers = watchPartyService.getInvitedUsers(watchPartyId);
        return ResponseEntity.ok(invitedUsers);
    }

    @GetMapping("/{watchPartyId}/invite-response")
    public ResponseEntity<String> handleInviteResponse(
            @PathVariable Long watchPartyId,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replaceFirst("^Bearer\\s+", "");
        User current = userService.getUserByToken(token);
        String username = current.getUsername();

        boolean updated = watchPartyService.updateInviteStatus(watchPartyId, username, status);
        return updated
                ? ResponseEntity.ok("Invite response recorded successfully!")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to update invite response.");
    }

    @GetMapping("/{watchPartyId}/latest-invite-status")
    public ResponseEntity<List<String>> getLatestInviteResponses(@PathVariable Long watchPartyId) {
        List<String> latestResponses = watchPartyService.getLatestInviteResponses(watchPartyId);
        return ResponseEntity.ok(latestResponses);
    }

    @GetMapping("/{watchPartyId}")
    public ResponseEntity<WatchParty> getWatchParty(@PathVariable Long watchPartyId) {
        WatchParty wp = watchPartyService.getWatchPartyById(watchPartyId);
        if (wp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "WatchParty not found.");
        }
        return ResponseEntity.ok(wp);
    }

    @GetMapping("")
    public ResponseEntity<List<WatchParty>> getWatchParties(
            @RequestParam(required = false) Long organizerId,
            @RequestParam(required = false) String username) {

        if (organizerId != null) {
            return ResponseEntity.ok(watchPartyService.getWatchPartiesByOrganizer(organizerId));
        }
        if (username != null) {
            return ResponseEntity.ok(watchPartyService.getWatchPartiesForInvitee(username));
        }
        return ResponseEntity.ok(watchPartyService.getAllWatchParties());
    }
}
