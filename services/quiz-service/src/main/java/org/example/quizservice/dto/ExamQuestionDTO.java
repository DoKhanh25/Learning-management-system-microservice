package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    private Long id;
    private Long examId;
    private Long questionId;
    private QuestionDTO question;
    private Integer questionOrder;
    private Float points;
}
