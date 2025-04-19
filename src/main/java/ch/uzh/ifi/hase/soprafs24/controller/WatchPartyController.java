package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * WatchPartyController - Manages watch party operations and invite-related functionality.
 */
@RestController
@RequestMapping("/api/watchparties")
public class WatchPartyController {

    private final WatchPartyService watchPartyService;
    private final UserService userService;

    @Autowired
    public WatchPartyController(WatchPartyService watchPartyService, UserService userService) {
        this.watchPartyService = watchPartyService;
        this.userService = userService;
    }

    // Existing functionality: Create a watch party
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

    // Existing functionality: Get watch parties (all or by organizer ID)
    @GetMapping("")
    public List<WatchParty> getWatchParties(@RequestParam(required = false) Long organizerId) {
        if (organizerId != null) {
            return watchPartyService.getWatchPartiesByOrganizer(organizerId);
        } else {
            return watchPartyService.getAllWatchParties();
        }
    }

    // New functionality: Invite a user to a watch party
    @PostMapping("/{watchPartyId}/invites")
    public ResponseEntity<Map<String, String>> inviteUser(
            @PathVariable Long watchPartyId,
            @RequestParam String username,
            @RequestParam Long inviterId) {
    
        String responseMessage = watchPartyService.inviteUserToWatchParty(watchPartyId, username, inviterId);
    
        Map<String, String> response = Map.of("message", responseMessage);
        return responseMessage.equals("Username does not exist")
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
                : ResponseEntity.ok(response);
    }    

    // New functionality: Fetch the list of invited users for a specific watch party
    @GetMapping("/{watchPartyId}/invites")
    public ResponseEntity<List<String>> getInvitedUsers(@PathVariable Long watchPartyId) {
        List<String> invitedUsers = watchPartyService.getInvitedUsers(watchPartyId);
        return new ResponseEntity<>(invitedUsers, HttpStatus.OK);
    }

    // New functionality: Handle invite response (accept or decline)
    @GetMapping("/{watchPartyId}/invite-response")
    public ResponseEntity<String> handleInviteResponse(
            @PathVariable Long watchPartyId,
            @RequestParam String username,
            @RequestParam String status) {

        boolean updated = watchPartyService.updateInviteStatus(watchPartyId, username, status);
        return updated
                ? ResponseEntity.ok("Invite response recorded successfully!")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to update invite response.");
    }

    // New functionality: Poll latest invite status updates
    @GetMapping("/{watchPartyId}/latest-invite-status")
    public ResponseEntity<List<String>> getLatestInviteResponses(@PathVariable Long watchPartyId) {
        List<String> latestResponses = watchPartyService.getLatestInviteResponses(watchPartyId);
        return new ResponseEntity<>(latestResponses, HttpStatus.OK);
    }
}
