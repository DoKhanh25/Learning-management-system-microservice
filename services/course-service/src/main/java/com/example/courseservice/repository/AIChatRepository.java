package com.example.courseservice.repository;

import com.example.courseservice.entity.AIChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AIChatRepository extends JpaRepository<AIChatEntity, Integer> {

    @Query("select ac from ai_chat ac where ac.session.id = :sessionId order by ac.messageOrder asc")
    List<AIChatEntity> findAiChatEntitiesBySessionIdOrderByCreatedTimeAsc(Long sessionId);
}
