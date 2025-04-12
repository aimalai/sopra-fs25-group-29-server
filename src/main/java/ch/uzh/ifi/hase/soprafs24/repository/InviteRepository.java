package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

    // ✅ Fetch invites by Watch Party ID
    List<Invite> findByWatchPartyId(Long watchPartyId);

    // ✅ NEW: Fetch invite by Watch Party ID and Username
    List<Invite> findByWatchPartyIdAndUsername(Long watchPartyId, String username);
}
