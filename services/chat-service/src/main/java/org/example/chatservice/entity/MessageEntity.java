package org.example.chatservice.entity;

import lombok.Data;
import org.example.chatservice.enums.MessageStatus;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import java.util.Date;
import java.util.UUID;

@Data
@Table("messages")
public class MessageEntity {
    @PrimaryKeyColumn(name = "chat_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String chatId;
    @PrimaryKeyColumn(name = "message_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private UUID messageId;
    private UUID senderId;
    private String content;
    private Date timestamp;
    private MessageStatus status;
    public MessageEntity(String chatId, UUID senderId, String content) {
        this.chatId = chatId;
        this.messageId = UUID.randomUUID();
        this.senderId = senderId;
        this.content = content;
        this.timestamp = new Date();
        this.status = MessageStatus.SENT;
    }
}
