package org.example.chatservice.service;

import com.datastax.oss.driver.api.core.cql.BatchType;
import com.example.commondto.dto.ResultDTO;
import org.example.chatservice.dto.ChatGroupDTO;
import org.example.chatservice.dto.PrivateChatDTO;
import org.example.chatservice.entity.ChatGroupEntity;
import org.example.chatservice.entity.UserChatsEntity;
import org.example.chatservice.repository.ChatGroupRepository;
import org.example.chatservice.repository.MessageRepository;
import org.example.chatservice.entity.MessageEntity;
import org.example.chatservice.repository.UserChatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserChatsRepository userChatsRepository;

    @Autowired
    private ChatGroupRepository chatGroupRepository;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageEntity sendMessage(String chatId, String senderId, String content) {
        MessageEntity messageEntity = new MessageEntity(chatId, UUID.fromString(senderId), content);
        messageEntity = messageRepository.save(messageEntity);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, messageEntity);
        return messageEntity;
    }

    public ResponseEntity<ResultDTO> addGroupChat(UUID creatorId, ChatGroupDTO chatGroupDTO) {
        String chatId = "group_" + UUID.randomUUID().toString();
        Set<UUID> memberIds = chatGroupDTO.getMemberIds();

        ChatGroupEntity group = new ChatGroupEntity(chatId, chatGroupDTO.getGroupName(), creatorId, chatGroupDTO.getMemberIds());
        ResultDTO resultDTO = new ResultDTO();
        try {
            CassandraBatchOperations batchOps = cassandraTemplate.batchOps(BatchType.LOGGED);
            batchOps.insert(group);

            for (UUID userId : memberIds) {
                UserChatsEntity userChats = userChatsRepository.findById(userId)
                        .orElse(new UserChatsEntity(userId, new HashSet<>()));
                userChats.getChatIds().add(chatId);
                batchOps.update(userChats);
            }

            if (!memberIds.contains(creatorId)) {
                UserChatsEntity creatorChats = userChatsRepository.findById(creatorId)
                        .orElse(new UserChatsEntity(creatorId, new HashSet<>()));
                creatorChats.getChatIds().add(chatId);
                batchOps.update(creatorChats);
            }

            // Execute batch
            batchOps.execute();

            resultDTO.setMessage("Success");
            resultDTO.setStatus(1);
            resultDTO.setData(group);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } catch (Exception e) {
            resultDTO.setMessage("Error: " + e.getMessage());
            resultDTO.setStatus(0);
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResultDTO> getRecentMessages(String chatId, String userId, String lastMessageId, int pageSize) {
        ResultDTO resultDTO = new ResultDTO();
        pageSize = pageSize > 0 ? pageSize : 10;
        if(chatId.isEmpty() || userId.isEmpty()){
            resultDTO.setStatus(0);
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        // validate
        if(chatId.contains("one_")){
            if(!chatId.contains(userId)){
                resultDTO.setStatus(0);
                resultDTO.setMessage("You are not authorized to view this chat.");
                return new ResponseEntity<>(resultDTO, HttpStatus.UNAUTHORIZED);
            }
        } else {
            ChatGroupEntity chatGroupEntity = chatGroupRepository.findByChatId(chatId);
            if(chatGroupEntity == null){
                resultDTO.setStatus(0);
                resultDTO.setMessage("You are not authorized to view this chat.");
                return new ResponseEntity<>(resultDTO, HttpStatus.UNAUTHORIZED);
            }
            Set<UUID> memberIds = chatGroupEntity.getMemberIds();
            if(!memberIds.contains(UUID.fromString(userId))){
                resultDTO.setStatus(0);
                resultDTO.setMessage("You are not authorized to view this chat.");
                return new ResponseEntity<>(resultDTO, HttpStatus.UNAUTHORIZED);
            }
        }

        List<MessageEntity> messages;
        if (lastMessageId == null) {
            // First page
            messages = messageRepository.findMessagesByChatIdWithLimit(chatId, pageSize);
        } else {
            // Subsequent pages
            messages = messageRepository.findMessagesByChatIdAndMessageIdLessThan(chatId, UUID.fromString(lastMessageId), pageSize);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(messages);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getUserChatList(UUID userId) {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        Set<String> userChatList = userChatsRepository.findById(userId)
                .map(UserChatsEntity::getChatIds)
                .orElse(Collections.emptySet());
        resultDTO.setData(userChatList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> addPrivateChat(PrivateChatDTO privateChatDTO) {
        String userId1 = privateChatDTO.getUserId1();
        String userId2 = privateChatDTO.getUserId2();
        ResultDTO resultDTO = new ResultDTO();

        if(userId1.isEmpty() || userId2.isEmpty()){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Both are required.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if(userId1.equals(userId2)){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Both must be different.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }


        List<String> userIds = Arrays.asList(userId1, userId2);
        resultDTO.setStatus(1);

        Collections.sort(userIds);
        String chatId = "one_" + userIds.get(0) + "_" + userIds.get(1);

        CassandraBatchOperations batchOps = cassandraTemplate.batchOps(BatchType.LOGGED);

        UserChatsEntity userChats1 = userChatsRepository.findById(UUID.fromString(userId1))
                .orElse(new UserChatsEntity(UUID.fromString(userId1), new HashSet<>()));
        userChats1.getChatIds().add(chatId);

        UserChatsEntity userChats2 = userChatsRepository.findById(UUID.fromString(userId2))
                .orElse(new UserChatsEntity(UUID.fromString(userId2), new HashSet<>()));
        userChats2.getChatIds().add(chatId);

        batchOps.insert(userChats1);
        batchOps.insert(userChats2);
        batchOps.execute();

        resultDTO.setMessage("Success");
        resultDTO.setData(chatId);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getGroupDetails(String chatId, String userId) {
        ResultDTO resultDTO = new ResultDTO();
        ChatGroupEntity group = chatGroupRepository.findById(chatId).orElse(null);
        if(group == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("No such chat group.");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        Set<UUID> memberIds = group.getMemberIds();
        if(memberIds.isEmpty() || !memberIds.contains(UUID.fromString(userId))){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You are not authorized to view this chat.");
            return new ResponseEntity<>(resultDTO, HttpStatus.UNAUTHORIZED);
        }
        resultDTO.setStatus(1);
        resultDTO.setData(group);
        return ResponseEntity.ok(resultDTO);
    }

}
