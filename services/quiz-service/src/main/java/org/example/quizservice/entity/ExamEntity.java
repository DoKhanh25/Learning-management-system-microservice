package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import org.example.quizservice.enums.ExamType;

import java.util.Date;
import java.util.List;

@Entity(name = "exam")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ExamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "course_id", nullable = false)
    Long courseId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    ExamType examType;

    @Column(name = "duration")
    Long duration;

    @Column(name = "total_score")
    Float totalScore;

    @Column(name = "shuffle_questions")
    Boolean shuffleQuestions;

    @Column(name = "shuffle_answers")
    Boolean shuffleAnswers;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "start_time")
    Date startTime;

    @Column(name = "end_time")
    Date endTime;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExamQuestionEntity> examQuestions;

}
