package org.example.quizservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MultipleChoiceSubmissionDTO extends QuestionSubmissionDTO {
    private List<Long> selectedOptionIds;
}
