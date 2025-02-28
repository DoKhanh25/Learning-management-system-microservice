package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity(name = "lesson_timer")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LessonTimerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "user_id")
    String userId;

    @Column(name = "start_time")
    Date startTime;

    @Column(name = "lesson_time")
    Long lessonTime;

    @Column(name = "completed")
    short completed;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonEntity lesson;

}
