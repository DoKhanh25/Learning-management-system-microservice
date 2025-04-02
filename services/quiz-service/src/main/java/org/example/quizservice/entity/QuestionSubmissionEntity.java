package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Entity(name = "question_submission")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class QuestionSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "exam_submission_id")
    private ExamSubmissionEntity examSubmission;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuestionEntity question;
    
    @Column(name = "score")
    private Float score;
    
    @Column(name = "submitted_at")
    private Date submittedAt;
    
    @Column(name = "graded")
    private Boolean graded;
    
    @Column(name = "feedback", columnDefinition = "LONGTEXT")
    private String feedback;
}