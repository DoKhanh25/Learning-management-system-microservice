package com.example.courseservice.repository;

import com.example.courseservice.entity.AIChatEntity;
import com.example.courseservice.entity.AIChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AIChatSessionRepository extends JpaRepository<AIChatSessionEntity, Long> {

    @Query("select ais.messages from ai_session ais where ais.id = :sessionId")
    List<AIChatEntity> findAIChatEntitiesBySessionId(Long sessionId);

    @Query("select ais from ai_session ais where ais.lesson.id = :sessionId")
    AIChatSessionEntity findAIChatSessionByLessonId(@Param("sessionId") Long sessionId);


}
