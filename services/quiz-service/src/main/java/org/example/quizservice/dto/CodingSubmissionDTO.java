package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CodingSubmissionDTO extends QuestionSubmissionDTO {
    private String submittedCode;
    private String testResults;
    private String compilationOutput;
    private Long executionTime;
}
