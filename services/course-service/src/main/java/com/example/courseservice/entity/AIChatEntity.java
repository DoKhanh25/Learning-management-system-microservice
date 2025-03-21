package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;


@Data
@Entity(name = "ai_chat")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AIChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    AIChatSessionEntity session;

    @Column(name = "user_message", columnDefinition = "LONGTEXT")
    String userMessage;

    @Column(name = "gemini_response", columnDefinition = "LONGTEXT")
    String geminiResponse;

    @Column(name = "message_order")
    Integer messageOrder;

    @Column(name = "created_time")
    Date createdTime;

}
