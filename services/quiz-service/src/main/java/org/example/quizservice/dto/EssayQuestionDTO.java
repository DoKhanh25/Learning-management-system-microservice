package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EssayQuestionDTO extends QuestionDTO {
    private Integer wordLimit;
    private String sampleAnswer;
    private String rubric;
}
