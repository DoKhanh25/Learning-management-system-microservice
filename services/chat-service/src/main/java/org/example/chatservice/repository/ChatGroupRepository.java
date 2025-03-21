package org.example.chatservice.repository;

import org.example.chatservice.entity.ChatGroupEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ChatGroupRepository extends CassandraRepository<ChatGroupEntity, String> {
}
