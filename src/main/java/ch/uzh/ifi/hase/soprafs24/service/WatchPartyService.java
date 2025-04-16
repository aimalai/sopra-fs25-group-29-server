package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class WatchPartyService {

    private final WatchPartyRepository watchPartyRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;

    @Autowired
    public WatchPartyService(WatchPartyRepository watchPartyRepository,
                              UserRepository userRepository,
                              InviteRepository inviteRepository) {
        this.watchPartyRepository = watchPartyRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
    }

    /**
     * Create a new watch party.
     * @param name - Name of the watch party.
     * @param id - ID of the organizer (user).
     * @param contentLink - Link to the movie/content being watched.
     * @return The created WatchParty object.
     */
    public WatchParty createWatchParty(String name, Long id, String contentLink) {
        if (id == null) {
            throw new IllegalArgumentException("Organizer ID cannot be null");
        }

        WatchParty watchParty = new WatchParty();
        watchParty.setName(name);
        watchParty.setOrganizerId(id);
        watchParty.setContentLink(contentLink);

        return watchPartyRepository.save(watchParty);
    }

    /**
     * Get all watch parties created by a specific user.
     * @param id - ID of the organizer (user).
     * @return List of watch parties.
     */
    public List<WatchParty> getWatchPartiesByOrganizer(Long id) {
        return watchPartyRepository.findByOrganizerId(id);
    }

    /**
     * Fetch the user's ID from the database using their username.
     * @param username - The username of the logged-in user.
     * @return The user's ID from the database, or null if not found.
     */
    public Long getUserIdByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }

    /**
     * Invite a user to a watch party and store the invite in the database.
     * @param watchPartyId - ID of the watch party.
     * @param username - Username of the user to invite.
     * @param inviterId - ID of the inviter.
     */
    public String inviteUserToWatchParty(Long watchPartyId, String username, Long inviterId) {
        Optional<User> userToInvite = userRepository.findByUsername(username);
        if (userToInvite.isEmpty()) {
            return "Username does not exist"; // User not found
        }

        Invite invite = new Invite();
        invite.setWatchPartyId(watchPartyId);
        invite.setUsername(username);
        invite.setStatus("pending");

        inviteRepository.save(invite);

        // ✅ Send email using Google SMTP
        sendInviteEmail(userToInvite.get().getEmail(), watchPartyId, username);

        return "Invite sent successfully!";
    }

    /**
     * Send an email invite via Google SMTP with accept/decline options.
     * @param email - Email of the recipient.
     * @param watchPartyId - Watch party ID.
     * @param username - Username of the invited user.
     */
    private void sendInviteEmail(String email, Long watchPartyId, String username) {
        String acceptLink = "http://localhost:8080/watchparty/" + watchPartyId + "/invite-response?username=" + username + "&status=accepted";
        String declineLink = "http://localhost:8080/watchparty/" + watchPartyId + "/invite-response?username=" + username + "&status=declined";

        String messageBody = "You've been invited to a watch party!\n\nClick below to respond:\n" +
                             "✅ Accept: " + acceptLink + "\n" +
                             "❌ Decline: " + declineLink;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); 
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); 
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication("sopragroup29@gmail.com", "nedniuwaejizktfg"); 
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("your-email@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("You're Invited to a Watch Party!");
            message.setText(messageBody);

            jakarta.mail.Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch list of invited users from the database.
     * @param watchPartyId - ID of the watch party.
     * @return List of invited users.
     */
    public List<String> getInvitedUsers(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(Invite::getUsername)
                .toList();
    }

    /**
     * Update invite response status (Accepted or Declined).
     * @param watchPartyId - Watch party ID.
     * @param username - Username of the invited user.
     * @param status - "accepted" or "declined".
     * @return true if updated, false otherwise.
     */
     public boolean updateInviteStatus(Long watchPartyId, String username, String status) {
        List<Invite> invites = inviteRepository.findByWatchPartyIdAndUsername(watchPartyId, username);
        
        if (!invites.isEmpty()) {
            for (Invite invite : invites) {
                invite.setStatus(status); // Update each invite
                inviteRepository.save(invite);
            }
            return true; // Successfully updated
        }
        return false; // No invites found
    }
    /**
     * 🔥 New method for polling latest invite responses.
     * @param watchPartyId - Watch Party ID.
     * @return List of updated invite statuses.
     */
    public List<String> getLatestInviteResponses(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(invite -> invite.getUsername() + " - " + invite.getStatus()) // ✅ Fetch username & status
                .toList();
    }
}
