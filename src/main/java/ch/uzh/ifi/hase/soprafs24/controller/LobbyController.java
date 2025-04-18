package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Map<String, Boolean>> roomStates = new ConcurrentHashMap<>();

    public LobbyController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join")
    public void handleJoin(@Payload PartyMessage msg, SimpMessageHeaderAccessor header) {
        String roomId    = msg.getRoomId();
        String sessionId = header.getSessionId();

        roomStates
          .computeIfAbsent(roomId, rid -> new ConcurrentHashMap<>())
          .put(sessionId, false);

        messagingTemplate.convertAndSend(
            "/topic/syncReadyState/" + roomId,
            roomStates.get(roomId)
        );
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
        String roomId    = msg.getRoomId();
        String sessionId = header.getSessionId();
        Map<String, Boolean> states = roomStates.get(roomId);
        if (states != null) {
            states.put(sessionId, ready);
            messagingTemplate.convertAndSend(
                "/topic/syncReadyState/" + roomId,
                states
            );
        }
    }

    public void removeSession(String sessionId) {
        roomStates.forEach((roomId, states) -> {
            if (states.remove(sessionId) != null) {
                messagingTemplate.convertAndSend(
                    "/topic/syncReadyState/" + roomId,
                    states
                );
            }
        });
    }

    public static class PartyMessage {
        private String roomId;
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
    }
}
