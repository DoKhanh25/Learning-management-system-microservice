package org.example.chatservice.repository;

import org.example.chatservice.entity.MessageEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CassandraRepository<MessageEntity, String>
{
    @Query("SELECT * FROM messages WHERE chat_id = ?0 LIMIT 50")
    List<MessageEntity> findRecentMessagesByChatId(String chatId);

    // Pagination using message ID as continuation token
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND message_id < :lastMessageId LIMIT :limit")
    List<MessageEntity> findMessagesByChatIdAndMessageIdLessThan(@Param("chatId") String chatId,
                                                                 @Param("lastMessageId") UUID lastMessageId,
                                                                 @Param("limit") int limit);

    // First page of messages
    @Query("SELECT * FROM messages WHERE chat_id = :chatId LIMIT :limit")
    List<MessageEntity> findMessagesByChatIdWithLimit(@Param("chatId") String chatId,
                                                      @Param("limit") int limit);
}
