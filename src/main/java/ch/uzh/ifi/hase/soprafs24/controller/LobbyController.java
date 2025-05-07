package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Map<String, Boolean>> roomStates = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsernames = new ConcurrentHashMap<>();
    private final Map<String, String> roomHosts = new ConcurrentHashMap<>();

    public LobbyController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageExceptionHandler(Exception.class)
    public void handleStompError(Exception ex, Message<?> message) {
        String destination = (String) message.getHeaders().get("simpDestination");
        String[] parts = destination.split("/");
        if (parts.length >= 3) {
            String roomId = parts[2];
            messagingTemplate.convertAndSend(
                "/topic/errors/" + roomId,
                ex.getMessage()
            );
        }
    }

    public static class ParticipantMessage {

        private String username;
        private boolean ready;

        public ParticipantMessage(String username, boolean ready) {
            this.username = username;
            this.ready = ready;
        }

        public String getUsername() {
            return username;
        }

        public boolean isReady() {
            return ready;
        }
    }

    public static class LobbyState {

        private List<ParticipantMessage> participants;
        private String hostUsername;

        public List<ParticipantMessage> getParticipants() {
            return participants;
        }

        public void setParticipants(List<ParticipantMessage> p) {
            this.participants = p;
        }

        public String getHostUsername() {
            return hostUsername;
        }

        public void setHostUsername(String h) {
            this.hostUsername = h;
        }
    }

    public static class TimeMessage {

        private String roomId;
        private double currentTime;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public double getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(double t) {
            this.currentTime = t;
        }
    }

    @MessageMapping("/join")
    public void handleJoin(@Payload PartyMessage msg, SimpMessageHeaderAccessor header) {
        String roomId = msg.getRoomId();
        String sessionId = header.getSessionId();
        String user = msg.getSender();

        sessionUsernames.put(sessionId, user);
        roomHosts.computeIfAbsent(roomId, rid -> sessionId);
        roomStates
            .computeIfAbsent(roomId, rid -> new ConcurrentHashMap<>())
            .put(sessionId, false);

        broadcastLobbyState(roomId);
    }

    @MessageMapping("/ready")
    public void handleReady(@Payload PartyMessage msg, SimpMessageHeaderAccessor header) {
        updateState(msg, header, true);
    }

    @MessageMapping("/notReady")
    public void handleNotReady(@Payload PartyMessage msg, SimpMessageHeaderAccessor header) {
        updateState(msg, header, false);
    }

    private void updateState(PartyMessage msg, SimpMessageHeaderAccessor header, boolean ready) {
        String roomId = msg.getRoomId();
        String sessionId = header.getSessionId();
        Map<String, Boolean> states = roomStates.get(roomId);
        if (states != null) {
            states.put(sessionId, ready);
            broadcastLobbyState(roomId);
        }
    }

    private void broadcastLobbyState(String roomId) {
        Map<String, Boolean> states = roomStates.get(roomId);
        if (states == null) {
            return;
        }

        List<ParticipantMessage> participants = states.entrySet().stream()
            .map(e -> new ParticipantMessage(
                sessionUsernames.getOrDefault(e.getKey(), "Unknown"),
                e.getValue()))
            .collect(Collectors.toList());

        String hostSession = roomHosts.get(roomId);
        String hostUser = sessionUsernames.getOrDefault(hostSession, "Unknown");

        LobbyState state = new LobbyState();
        state.setParticipants(participants);
        state.setHostUsername(hostUser);

        messagingTemplate.convertAndSend(
            "/topic/syncReadyState/" + roomId,
            state
        );
    }

    @MessageMapping("/shareTime")
    public void handleShareTime(@Payload TimeMessage msg) {
        messagingTemplate.convertAndSend(
            "/topic/syncTime/" + msg.getRoomId(),
            msg
        );
    }

    @MessageMapping("/chat.sendLobbyMessage")
    public void handleLobbyChat(@Payload LobbyChatMessage msg) {
        msg.setTimestamp(java.time.LocalDateTime.now());
        messagingTemplate.convertAndSend(
            "/topic/chat/" + msg.getRoomId(),
            msg
        );
    }

    public void removeSession(String sessionId) {
        roomStates.forEach((roomId, states) -> {
            if (states.remove(sessionId) != null) {
                broadcastLobbyState(roomId);
            }
        });
    }

    public static class LobbyChatMessage {

        private String roomId, sender, content;
        private java.time.LocalDateTime timestamp;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(java.time.LocalDateTime t) {
            this.timestamp = t;
        }
    }

    public static class PartyMessage {

        private String roomId, sender;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }
    }
}
