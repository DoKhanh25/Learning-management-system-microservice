package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleChoiceOptionDTO {
    private Long id;
    private String text;
    private Boolean isCorrect;
    private Integer displayOrder;
    private Long questionId;
}
