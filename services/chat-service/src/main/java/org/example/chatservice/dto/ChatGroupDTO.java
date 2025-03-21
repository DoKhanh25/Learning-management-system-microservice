package org.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupDTO {
    private String chatId;
    private String groupName;
    private UUID creatorId;
    private Set<UUID> memberIds;
    private Date createdAt;
}
