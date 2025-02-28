package com.example.courseservice.entity;

import com.example.courseservice.enums.QType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity(name = "lesson_pages")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LessonPagesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    LessonEntity lesson;


    @OneToMany(mappedBy = "lessonPages", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<LessonAttemptsEntity> lessonAttempts;

    @OneToMany(mappedBy = "lessonPages", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<LessonBranchEntity> lessonBranch;

    @Column(name = "position")
    Integer position;

    @Column(name = "qtype")
    QType qType;

    @Column(name = "title")
    String title;

    @Column(name = "content")
    String content;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "updated_time")
    Date updatedTime;

}
