package org.example.chatservice.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@Table("chat_groups")
public class ChatGroupEntity {
    @PrimaryKey
    private String chatId;
    private String groupName;
    private UUID creatorId;
    private Set<UUID> memberIds;
    private Date createdAt;

    public ChatGroupEntity(String chatId, String groupName, UUID creatorId, Set<UUID> memberIds) {
        this.chatId = chatId;
        this.groupName = groupName;
        this.creatorId = creatorId;
        this.memberIds = memberIds;
        this.createdAt = new Date();
    }
}
