package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "exam_question")
@Data
public class ExamQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private ExamEntity exam;
    
    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuestionEntity question;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    @Column(name = "points")
    private Float points;
}
