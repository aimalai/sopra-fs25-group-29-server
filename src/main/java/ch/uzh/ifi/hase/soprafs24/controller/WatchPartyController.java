package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.service.WatchPartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WatchPartyController - Manages watch party operations independently.
 */
@RestController
@RequestMapping("/watchparty")
public class WatchPartyController {

    private final WatchPartyService watchPartyService;

    public WatchPartyController(WatchPartyService watchPartyService) {
        this.watchPartyService = watchPartyService;
    }

    /**
     * Create a new watch party using the logged-in user's ID.
     * @param watchParty - Watch party details.
     * @return ResponseEntity with the created watch party.
     */
    @PostMapping("/create")
    public ResponseEntity<WatchParty> createWatchParty(@RequestBody WatchParty watchParty) {
        if (watchParty.getOrganizerId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        WatchParty newWatchParty = watchPartyService.createWatchParty(
                watchParty.getName(),
                watchParty.getOrganizerId(),
                watchParty.getContentLink()
        );

        return new ResponseEntity<>(newWatchParty, HttpStatus.CREATED);
    }

    /**
     * Get all watch parties linked to a user ID.
     * @param id - The user's ID.
     * @return List of watch parties.
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<List<WatchParty>> getUserWatchParties(@PathVariable Long id) {
        List<WatchParty> watchParties = watchPartyService.getWatchPartiesByOrganizer(id);
        return new ResponseEntity<>(watchParties, HttpStatus.OK);
    }

    /**
     * Fetch the user's ID from the database using their username.
     * @param username - The username of the logged-in user.
     * @return ResponseEntity with the user's ID.
     */
    @GetMapping("/user-id/{username}")
    public ResponseEntity<Long> getUserId(@PathVariable String username) {
        Long userId = watchPartyService.getUserIdByUsername(username);
        return userId != null
                ? new ResponseEntity<>(userId, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Invite a user to a watch party.
     * @param watchPartyId - Watch Party ID.
     * @param username - Username to invite.
     * @param inviterId - ID of inviter.
     * @return ResponseEntity with success/failure status.
     */
    @PostMapping("/{watchPartyId}/invite")
    public ResponseEntity<String> inviteUser(
        @PathVariable Long watchPartyId,
        @RequestParam String username,
        @RequestParam Long inviterId) {

        String responseMessage = watchPartyService.inviteUserToWatchParty(watchPartyId, username, inviterId);

        return responseMessage.equals("Username does not exist")
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMessage)
                : ResponseEntity.ok(responseMessage);
    }

    /**
     * Fetch the list of invited users for a specific watch party from the database.
     * @param watchPartyId - Watch Party ID.
     * @return List of invited users.
     */
    @GetMapping("/{watchPartyId}/invites")
    public ResponseEntity<List<String>> getInvitedUsers(@PathVariable Long watchPartyId) {
        List<String> invitedUsers = watchPartyService.getInvitedUsers(watchPartyId);
        return new ResponseEntity<>(invitedUsers, HttpStatus.OK);
    }

    /**
     * Handle invite response clicks (accept or decline).
     * @param watchPartyId - Watch Party ID.
     * @param username - Username responding.
     * @param status - "accepted" or "declined".
     * @return ResponseEntity with confirmation.
     */
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

    /**
     * 🔥 New API for polling latest invite status.
     * @param watchPartyId - Watch Party ID.
     * @return JSON with latest invite response updates.
     */
    @GetMapping("/{watchPartyId}/latest-invite-status")
    public ResponseEntity<List<String>> getLatestInviteResponses(@PathVariable Long watchPartyId) {
        List<String> latestResponses = watchPartyService.getLatestInviteResponses(watchPartyId);
        return new ResponseEntity<>(latestResponses, HttpStatus.OK);
    }
}
