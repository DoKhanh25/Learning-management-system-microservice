package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizservice.enums.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String text;
    private Integer points;
    private String difficultyLevel;
    private QuestionType questionType;
    private String userId;
    private Long questionBankId;
}
