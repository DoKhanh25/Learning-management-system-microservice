package com.example.courseservice.entity;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity(name = "ai_session")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AIChatSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<AIChatEntity> messages;

    @Column(name = "context_used", columnDefinition = "LONGTEXT")
    String contextUsed;

    @OneToOne(fetch = FetchType.LAZY)
    LessonEntity lesson;

    @Column(name = "session_name")
    String sessionName;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "updated_time")
    Date updatedTime;
}
