package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Invite;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.WatchParty;
import ch.uzh.ifi.hase.soprafs24.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.WatchPartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class WatchPartyService {

    private final WatchPartyRepository watchPartyRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    @Value("${app.backend.base-url}")
    private String baseUrl;


    @Autowired
    public WatchPartyService(WatchPartyRepository watchPartyRepository,
                             UserRepository userRepository,
                             InviteRepository inviteRepository) {
        this.watchPartyRepository = watchPartyRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
    }

    // Existing functionality: Create a new watch party
    public WatchParty createWatchParty(User organizer, String title, String contentLink, String description, LocalDateTime scheduledTime) {
        ZonedDateTime scheduledTimeUTC = scheduledTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime scheduledTimeLocal = scheduledTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
        if (scheduledTimeLocal.isBefore(ZonedDateTime.now(ZoneId.systemDefault()))) {
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

    // Existing functionality: Get watch parties by organizer ID
    public List<WatchParty> getWatchPartiesByOrganizer(Long organizerId) {
        return watchPartyRepository.findByOrganizer_Id(organizerId);
    }

    // Existing functionality: Get all watch parties
    public List<WatchParty> getAllWatchParties() {
        return watchPartyRepository.findAll();
    }


// New functionality: Invite a user to a watch party
public String inviteUserToWatchParty(Long watchPartyId, String username, Long inviterId) {
    System.out.println("Attempting to find user by username: " + username);

    // Fetch user from the repository
    User userToInvite = userRepository.findByUsername(username);

    if (userToInvite == null) { // Adjusted to handle null return
        System.out.println("User not found in database: " + username);
        return "Username does not exist"; // User not found
    }

    System.out.println("User found: " + userToInvite.getUsername() + ", Email: " + userToInvite.getEmail());

    // Create a new invite object
    Invite invite = new Invite();
    invite.setWatchPartyId(watchPartyId);
    invite.setUsername(username);
    invite.setStatus("pending");

    // Save invite to the repository
    inviteRepository.save(invite);
    System.out.println("Invite created and saved for user: " + username);

    // Send email invite using the user's email
    sendInviteEmail(userToInvite.getEmail(), watchPartyId, username);
    System.out.println("Invite email sent to: " + userToInvite.getEmail());

    return "Invite sent successfully!";
}


    // New functionality: Send email invite via SMTP
    private void sendInviteEmail(String email, Long watchPartyId, String username) {
        String acceptLink = baseUrl + "/api/watchparties/" + watchPartyId + "/invite-response?username=" + username + "&status=accepted";
        String declineLink = baseUrl + "/api/watchparties/" + watchPartyId + "/invite-response?username=" + username + "&status=declined";
        //String acceptLink = "http://localhost:8080/api/watchparties/" + watchPartyId + "/invite-response?username=" + username + "&status=accepted";
        //String declineLink = "http://localhost:8080/api/watchparties/" + watchPartyId + "/invite-response?username=" + username + "&status=declined";

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
            message.setFrom(new InternetAddress("sopragroup29@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("You're Invited to a Watch Party!");
            message.setText(messageBody);

            jakarta.mail.Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // New functionality: Fetch list of invited users
    public List<String> getInvitedUsers(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(Invite::getUsername)
                .toList();
    }

    // New functionality: Update invite response status
    public boolean updateInviteStatus(Long watchPartyId, String username, String status) {
        List<Invite> invites = inviteRepository.findByWatchPartyIdAndUsername(watchPartyId, username);

        if (!invites.isEmpty()) {
            for (Invite invite : invites) {
                invite.setStatus(status);
                inviteRepository.save(invite);
            }
            return true;
        }
        return false;
    }

    // New functionality: Fetch latest invite responses
    public List<String> getLatestInviteResponses(Long watchPartyId) {
        return inviteRepository.findByWatchPartyId(watchPartyId)
                .stream()
                .map(invite -> invite.getUsername() + " - " + invite.getStatus())
                .toList();
    }
}
