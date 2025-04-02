package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "coding_question")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "question_id")
public class CodingQuestionEntity extends QuestionEntity {
    
    @Column(name = "programming_language")
    private String programmingLanguage;
    
    @Column(name = "starter_code", columnDefinition = "LONGTEXT")
    private String starterCode;
    
    @Column(name = "solution_code", columnDefinition = "LONGTEXT")
    private String solutionCode;
    
    @Column(name = "test_cases", columnDefinition = "LONGTEXT")
    private String testCases;
}
