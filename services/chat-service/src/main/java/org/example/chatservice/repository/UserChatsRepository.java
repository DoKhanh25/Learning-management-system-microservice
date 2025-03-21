package org.example.chatservice.repository;

import org.example.chatservice.entity.UserChatsEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface UserChatsRepository extends CassandraRepository<UserChatsEntity, UUID> {

}
