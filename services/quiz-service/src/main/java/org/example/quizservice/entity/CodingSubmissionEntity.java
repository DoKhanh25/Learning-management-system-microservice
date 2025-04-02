package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "coding_submission")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "submission_id")
public class CodingSubmissionEntity extends QuestionSubmissionEntity {
    
    @Column(name = "submitted_code", columnDefinition = "LONGTEXT")
    private String submittedCode;
    
    @Column(name = "test_results", columnDefinition = "LONGTEXT")
    private String testResults;
    
    @Column(name = "compilation_output", columnDefinition = "LONGTEXT")
    private String compilationOutput;
    
    @Column(name = "execution_time")
    private Long executionTime;
}