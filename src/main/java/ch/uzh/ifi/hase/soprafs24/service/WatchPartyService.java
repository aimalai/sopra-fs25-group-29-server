package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchPartyService {

    private final WatchPartyRepository watchPartyRepository;

    @Autowired
    public WatchPartyService(WatchPartyRepository watchPartyRepository) {
        this.watchPartyRepository = watchPartyRepository;
    }

    public WatchParty createWatchParty(User organizer, String title, String contentLink, String description, LocalDateTime scheduledTime) {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled time must be in the future.");
        }
        
        WatchParty watchParty = new WatchParty();
        watchParty.setOrganizer(organizer);
        watchParty.setTitle(title);
        watchParty.setContentLink(contentLink);
        watchParty.setDescription(description);
        watchParty.setScheduledTime(scheduledTime);

        return watchPartyRepository.save(watchParty);
    }
    
    public List<WatchParty> getWatchPartiesByOrganizer(Long organizerId) {
        return watchPartyRepository.findByOrganizer_Id(organizerId);
    }
    
    public List<WatchParty> getAllWatchParties() {
        return watchPartyRepository.findAll();
    }
}
