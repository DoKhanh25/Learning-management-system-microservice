package org.example.chatservice.configuration;

import com.datastax.oss.driver.api.core.CqlSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.example.chatservice.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {
    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String localDatacenter;


    @Override
    protected String getContactPoints() {
        return contactPoints;
    }
    @Override
    protected int getPort() {
        return port;
    }

    @Bean
    @Primary
    public CqlSession session() {
        // Tạo keyspace nếu chưa tồn tại
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new java.net.InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(localDatacenter)
                .build()) {
            session.execute(
                    "CREATE KEYSPACE IF NOT EXISTS " + keyspaceName +
                            " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}"
            );
        }

        // Trả về session cho Spring Data
        return CqlSession.builder()
                .addContactPoint(new java.net.InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(localDatacenter)
                .withKeyspace(keyspaceName)
                .build();
    }

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

}
