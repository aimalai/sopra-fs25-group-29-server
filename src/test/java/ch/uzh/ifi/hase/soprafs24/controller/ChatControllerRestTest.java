package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@WebMvcTest(ChatController.class)
class ChatControllerRestTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @MockBean
    ChatMessageRepository repo;

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Test
    void getChatHistory_ok() throws Exception {
        when(userService.areFriends(1L, 2L)).thenReturn(true);
        ChatMessage msg = new ChatMessage();
        msg.setContent("hello");
        msg.setTimestamp(LocalDateTime.now());
        when(repo.findBySenderIdAndReceiverIdOrderByTimestampAsc(1L, 2L))
            .thenReturn(List.of(msg));
        when(repo.findBySenderIdAndReceiverIdOrderByTimestampAsc(2L, 1L))
            .thenReturn(List.of());

        mvc.perform(get("/chat/history/1/2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].content").value("hello"));
    }

    @Test
    void getChatHistory_forbidden() throws Exception {
        when(userService.areFriends(1L, 2L)).thenReturn(false);

        mvc.perform(get("/chat/history/1/2"))
           .andExpect(status().isForbidden());
    }
}
