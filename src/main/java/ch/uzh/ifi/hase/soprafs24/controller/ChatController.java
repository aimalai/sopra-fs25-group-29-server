package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        chatMessage.setTimestamp(java.time.LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }
    
    @GetMapping("/chat/history/{userId}/{friendId}")
    public List<ChatMessage> getChatHistory(@PathVariable Long userId, @PathVariable Long friendId) {
        List<ChatMessage> messages1 = chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(userId, friendId);
        List<ChatMessage> messages2 = chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(friendId, userId);
        List<ChatMessage> fullHistory = new ArrayList<>();
        fullHistory.addAll(messages1);
        fullHistory.addAll(messages2);
        fullHistory.sort(Comparator.comparing(ChatMessage::getTimestamp));
        return fullHistory;
    }
}
