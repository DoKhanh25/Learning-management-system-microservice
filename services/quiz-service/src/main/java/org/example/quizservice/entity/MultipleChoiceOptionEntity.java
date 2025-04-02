package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "multiple_choice_option")
@Data
public class MultipleChoiceOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "text", columnDefinition = "LONGTEXT")
    private String text;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    private MultipleChoiceQuestionEntity question;
}
