package ch.uzh.ifi.hase.soprafs24.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * EmailService - Handles sending OTP via Google SMTP
 */
@Service
public class EmailService {

    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}") 
    private String emailUsername;

    @Value("${spring.mail.password}") 
    private String emailPassword;

    public void sendOTP(String recipientEmail, String otp) {
        String subject = "Your OTP Code";
        String messageBody = "Your OTP code is: " + otp + "\nThis code expires in 5 minutes.";

        try {
            sendEmail(recipientEmail, subject, messageBody);
            log.info("OTP email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
        }
    }

public void sendEmail(String recipient, String subject, String body) throws MessagingException {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", "smtp.gmail.com");
    properties.put("mail.smtp.port", "587");
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.ssl.protocols", "TLSv1.2"); 
    properties.put("mail.smtp.ssl.trust", "smtp.gmail.com"); 
    
    // Debug to capture errors in logs
    Session session = Session.getInstance(properties, new Authenticator() {
        protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
            return new jakarta.mail.PasswordAuthentication(emailUsername, emailPassword);
        }
    });
    session.setDebug(true); 

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(emailUsername));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
    message.setSubject(subject);
    
    // Set HTML content
    message.setContent(body, "text/html");

    Transport.send(message);
}

}
