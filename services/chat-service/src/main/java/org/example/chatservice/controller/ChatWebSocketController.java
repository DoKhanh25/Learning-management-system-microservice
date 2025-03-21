package org.example.chatservice.controller;

import org.example.chatservice.dto.MessageDTO;
import org.example.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatWebSocketController {
    @Autowired
    private ChatService chatService;

    @MessageMapping("/send/{chatId}")
    public void handleMessage(@DestinationVariable String chatId, @Payload MessageDTO message) {
        chatService.sendMessage(chatId, message.getSenderId().toString(), message.getContent());
    }
}
