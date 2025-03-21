package org.example.chatservice.controller;


import com.example.commondto.dto.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.example.chatservice.dto.ChatGroupDTO;
import org.example.chatservice.dto.PrivateChatDTO;
import org.example.chatservice.dto.MessageDTO;
import org.example.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
public class ChatController {
    @Autowired
    private ChatService chatService;


    @GetMapping("/chat/recent-messages/{chatId}")
    public ResponseEntity<ResultDTO> findRecentMessagesByChatId(@PathVariable String chatId,
                                                                @RequestHeader("X-User-Id") String userId,
                                                                @RequestParam(required = false) String lastMessageId,
                                                                @RequestParam(required = false) Integer limit) {
        return chatService.getRecentMessages(chatId, userId, lastMessageId, limit);
    }

    @GetMapping("/chat/userChatList")
    public ResponseEntity<ResultDTO> findUserChatList(@RequestHeader("X-User-Id") String userId) {
        return chatService.getUserChatList(UUID.fromString(userId));
    }

    @PostMapping("/chat/addPrivateChat")
    public ResponseEntity<ResultDTO> addPrivateChat(@RequestBody PrivateChatDTO privateChatDTO) {
        log.info("Add private chat: {}", privateChatDTO.toString());
        return chatService.addPrivateChat(privateChatDTO);
    }

    @PostMapping("/chat/addGroupChat")
    public ResponseEntity<ResultDTO> addGroupChat(@RequestHeader("X-User-Id") String userId,
                                                   @RequestBody ChatGroupDTO chatGroupDTO) {
        return chatService.addGroupChat(UUID.fromString(userId), chatGroupDTO);
    }

//    @MessageMapping("/send/{chatId}")
//    public void handleMessage(@DestinationVariable String chatId, @Payload MessageDTO message) {
//        chatService.sendMessage(chatId, message.getSenderId().toString(), message.getContent());
//    }
}
