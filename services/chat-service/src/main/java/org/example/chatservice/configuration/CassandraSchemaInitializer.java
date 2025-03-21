package org.example.chatservice.configuration;


import jakarta.annotation.PostConstruct;
import org.example.chatservice.entity.ChatGroupEntity;
import org.example.chatservice.entity.MessageEntity;
import org.example.chatservice.entity.UserChatsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

@Configuration
public class CassandraSchemaInitializer {

    @Autowired
    private CassandraAdminTemplate cassandraAdminTemplate;

    @Autowired
    private CqlTemplate cqlTemplate;

    @PostConstruct
    public void initializeSchema() {
        cassandraAdminTemplate.createTable(true, ChatGroupEntity.class);
        cassandraAdminTemplate.createTable(true, MessageEntity.class);
        cassandraAdminTemplate.createTable(true, UserChatsEntity.class);
    }
}
