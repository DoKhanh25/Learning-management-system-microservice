package org.example.quizservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity(name = "multiple_choice_submission")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "submission_id")
public class MultipleChoiceSubmissionEntity extends QuestionSubmissionEntity {
    
    @ElementCollection
    @CollectionTable(
        name = "multiple_choice_selected_options",
        joinColumns = @JoinColumn(name = "submission_id")
    )
    @Column(name = "option_id")
    private List<Long> selectedOptionIds;
}