package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.List;

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

    @GetMapping("")
    public List<WatchParty> getWatchParties(@RequestParam(required = false) Long organizerId) {
        if (organizerId != null) {
            return watchPartyService.getWatchPartiesByOrganizer(organizerId);
        } else {
            return watchPartyService.getAllWatchParties();
        }
    }
}
