package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

    // fetch invites by Watch Party ID
    List<Invite> findByWatchPartyId(Long watchPartyId);

    // fetch invite by Watch Party ID and Username
    List<Invite> findByWatchPartyIdAndUsername(Long watchPartyId, String username);

    /**
     * New query for polling latest invite responses.
     * @param watchPartyId - Watch Party ID.
     * @return List of invites ordered by latest updates.
     */
    List<Invite> findByWatchPartyIdOrderByUpdatedAtDesc(Long watchPartyId); // using updatedAt for polling accuracy
}
