package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CodingQuestionDTO extends QuestionDTO {
    private String programmingLanguage;
    private String starterCode;
    private String solutionCode;
    private String testCases;
}
