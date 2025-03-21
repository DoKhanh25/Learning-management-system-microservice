package org.example.chatservice.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

@Data
@Table("user_chats")
public class UserChatsEntity {
    @PrimaryKey
    private UUID userId;
    private Set<String> chatIds;
    public UserChatsEntity(UUID userId, Set<String> chatIds) {
        this.userId = userId;
        this.chatIds = chatIds;
    }
}
