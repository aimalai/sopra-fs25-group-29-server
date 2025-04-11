package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WatchPartyRepository extends JpaRepository<WatchParty, Long> {
    List<WatchParty> findByOrganizer_Id(Long organizerId);
}
