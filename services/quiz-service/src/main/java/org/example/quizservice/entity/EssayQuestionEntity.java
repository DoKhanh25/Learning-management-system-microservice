package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "essay_question")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "question_id")
public class EssayQuestionEntity extends QuestionEntity {
    
    @Column(name = "word_limit")
    private Integer wordLimit;
    
    @Column(name = "sample_answer", columnDefinition = "LONGTEXT")
    private String sampleAnswer;
    
    @Column(name = "rubric", columnDefinition = "LONGTEXT")
    private String rubric;
}
