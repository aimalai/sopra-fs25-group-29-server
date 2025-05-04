package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.ChatMessage;
import ch.uzh.ifi.hase.soprafs24.repository.ChatMessageRepository;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@RestController
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        if (!userService.areFriends(chatMessage.getSenderId(), chatMessage.getReceiverId())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only chat with friends."
            );
        }
        chatMessage.setTimestamp(java.time.LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }
    
    @GetMapping("/chat/history/{userId}/{friendId}")
    public List<ChatMessage> getChatHistory(
            @PathVariable Long userId,
            @PathVariable Long friendId
    ) {
        if (!userService.areFriends(userId, friendId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only view chat with friends."
            );
        }

        List<ChatMessage> messages1 =
            chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(userId, friendId);
        List<ChatMessage> messages2 =
            chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(friendId, userId);

        List<ChatMessage> fullHistory = new ArrayList<>();
        fullHistory.addAll(messages1);
        fullHistory.addAll(messages2);
        fullHistory.sort(Comparator.comparing(ChatMessage::getTimestamp));

        return fullHistory;
    }

    @DeleteMapping("/chat/conversation/{userA}/{userB}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(
        @PathVariable Long userA,
        @PathVariable Long userB
    ) {
        chatMessageRepository.deleteBySenderIdAndReceiverId(userA, userB);
        chatMessageRepository.deleteBySenderIdAndReceiverId(userB, userA);
    }

}
