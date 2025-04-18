package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import ch.uzh.ifi.hase.soprafs24.controller.LobbyController;

@Component
public class WebSocketEventListener {

    private final LobbyController lobbyController;

    public WebSocketEventListener(LobbyController lobbyController) {
        this.lobbyController = lobbyController;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        lobbyController.removeSession(sessionId);
    }
}
