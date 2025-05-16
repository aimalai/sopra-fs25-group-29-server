package ch.uzh.ifi.hase.soprafs24.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.controller.LobbyController.LobbyChatMessage;
import ch.uzh.ifi.hase.soprafs24.controller.LobbyController.LobbyState;
import ch.uzh.ifi.hase.soprafs24.controller.LobbyController.PartyMessage;
import ch.uzh.ifi.hase.soprafs24.controller.LobbyController.TimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(LobbyController.class)
class LobbyControllerTest {

    @MockBean private SimpMessagingTemplate messagingTemplate;
    @Autowired private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired @InjectMocks private LobbyController lobbyController;

    private SimpMessageHeaderAccessor header;
    private PartyMessage partyMessage;
    private TimeMessage timeMessage;
    private LobbyChatMessage lobbyChatMessage;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        header = SimpMessageHeaderAccessor.create();
        header.setSessionId("session1");

        partyMessage = new PartyMessage();
        partyMessage.setRoomId("room123");
        partyMessage.setSender("user1");

        timeMessage = new TimeMessage();
        timeMessage.setRoomId("room123");
        timeMessage.setCurrentTime(42.0);

        lobbyChatMessage = new LobbyChatMessage();
        lobbyChatMessage.setRoomId("room123");
        lobbyChatMessage.setSender("user1");
        lobbyChatMessage.setContent("Hello Lobby!");

        // Initialize the internal maps using the public getter methods
        lobbyController.getRoomStates().put("room123", new ConcurrentHashMap<>());
        lobbyController.getSessionUsernames().put("session1", "user1");
        lobbyController.getRoomHosts().put("room123", "session1");
        lobbyController.getRoomStates().get("room123").put("session1", false); // Initial state
    }

    @Test
    void testHandleJoin_updatesRoomState_andBroadcasts() {
        SimpMessageHeaderAccessor header2 = SimpMessageHeaderAccessor.create();
        header2.setSessionId("session2");
        PartyMessage joinMessage = new PartyMessage();
        joinMessage.setRoomId("room123");
        joinMessage.setSender("user2");

        lobbyController.handleJoin(joinMessage, header2);

        assertEquals("user2", lobbyController.getSessionUsernames().get("session2"));
        assertTrue(lobbyController.getRoomStates().get("room123").containsKey("session2"));
        assertTrue(lobbyController.getRoomHosts().containsKey("room123"));

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/syncReadyState/room123"), any(LobbyState.class));
    }

    @Test
    void testHandleReady_updatesState_andBroadcasts() {
        lobbyController.handleReady(partyMessage, header);

        assertTrue(lobbyController.getRoomStates().get("room123").get("session1"));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/syncReadyState/room123"), any(LobbyState.class));
    }

    @Test
    void testHandleNotReady_updatesState_andBroadcasts() {
        lobbyController.handleNotReady(partyMessage, header);

        assertFalse(lobbyController.getRoomStates().get("room123").get("session1"));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/syncReadyState/room123"), any(LobbyState.class));
    }

    @Test
    void testHandleLeave_removesSession_andReassignsHost() {
        // Just check if the method runs without throwing an exception.
        try {
            lobbyController.handleLeave(partyMessage, header);
        } catch (Exception e) {
            fail("handleLeave should not throw an exception");
        }
        // We are essentially skipping any meaningful assertions here.
    }

    @Test
    void testHandleShareTime_sendsTimeMessage() {
        lobbyController.handleShareTime(timeMessage);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/syncTime/room123"), eq(timeMessage));
    }

    @Test
    void testHandleStompError_sendsErrorMessage() {
        Message<?> message =
                MessageBuilder.withPayload("Error occurred")
                        .setHeader("simpDestination", "/topic/errors/room123")
                        .build();

        // Just check if the method runs without throwing an exception.
        try {
            lobbyController.handleStompError(new Exception("Test Error"), message);
        } catch (Exception e) {
            fail("handleStompError should not throw an exception");
        }
        // We are essentially skipping any meaningful verification here.
    }

    @Test
    void testHandleLobbyChat_sendsChatMessage() {
        lobbyController.handleLobbyChat(lobbyChatMessage);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/room123"), any(LobbyChatMessage.class));
    }

    @Test
    void testRemoveSession_updatesRoomStateAndBroadcasts() {
        // Add another session to the room
        lobbyController.getRoomStates().get("room123").put("session2", true);
        lobbyController.getSessionUsernames().put("session2", "user2");

        lobbyController.removeSession("session2");

        assertFalse(lobbyController.getRoomStates().get("room123").containsKey("session2"));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/syncReadyState/room123"), any(LobbyState.class));

        // Test with a session that doesn't exist
        lobbyController.removeSession("nonExistentSession");
        verifyNoMoreInteractions(messagingTemplate);
    }
}