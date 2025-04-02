package org.example.quizservice.dto;

import lombok.Data;

@Data
public class ExamQuestionDTO {
    private Long id;
    private Long examId;
    private Long questionId;
    private Integer questionOrder;
    private Float points;
}
