package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "essay_submission")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "submission_id")
public class EssaySubmissionEntity extends QuestionSubmissionEntity {
    
    @Column(name = "answer_text", columnDefinition = "LONGTEXT")
    private String answerText;
    
    @Column(name = "word_count")
    private Integer wordCount;
}