package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MultipleChoiceQuestionDTO extends QuestionDTO {
    private Boolean allowMultipleAnswers;
    private List<MultipleChoiceOptionDTO> options;
}
