package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "lesson_branch")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LessonBranchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_pages", nullable = false)
    LessonPagesEntity lessonPages;

    @Column(name = "lesson_id")
    Long lessonId;

    @Column(name = "user_id")
    String userId;

    @Column(name = "time_seen")
    Long timeSeen;
}
