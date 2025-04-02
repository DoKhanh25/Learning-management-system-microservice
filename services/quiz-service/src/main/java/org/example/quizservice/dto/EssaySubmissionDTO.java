package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EssaySubmissionDTO extends QuestionSubmissionDTO {
    private String answerText;
    private Integer wordCount;
}
