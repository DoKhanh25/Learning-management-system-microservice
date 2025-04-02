package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity(name = "exam_submission")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ExamSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamEntity exam;
    
    @Column(name = "start_time")
    private Date startTime;
    
    @Column(name = "submission_time")
    private Date submissionTime;
    
    @Column(name = "total_score")
    private Float totalScore;
    
    @Column(name = "is_graded")
    private Boolean isGraded;
    
    @Column(name = "graded_by")
    private Long gradedBy;
    
    @Column(name = "grading_time")
    private Date gradingTime;
    
    @Column(name = "feedback", columnDefinition = "LONGTEXT")
    private String feedback;
    
    @OneToMany(mappedBy = "examSubmission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuestionSubmissionEntity> questionSubmissions;
}