package org.example.chatservice.repository;

import org.example.chatservice.entity.ChatGroupEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

public interface ChatGroupRepository extends CassandraRepository<ChatGroupEntity, String> {
    public ChatGroupEntity findByChatId(String chatId);
}
