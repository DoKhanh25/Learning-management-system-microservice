package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizservice.enums.QuestionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankDTO {
    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private String userId;
    private QuestionType questionType;
    private List<QuestionDTO> questions;
}
