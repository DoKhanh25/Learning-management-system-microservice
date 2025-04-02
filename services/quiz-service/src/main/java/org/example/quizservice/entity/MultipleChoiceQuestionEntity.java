package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity(name = "multiple_choice_question")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "question_id")
public class MultipleChoiceQuestionEntity extends QuestionEntity {
    
    @Column(name = "allow_multiple_answers")
    private Boolean allowMultipleAnswers;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<MultipleChoiceOptionEntity> options;
}
