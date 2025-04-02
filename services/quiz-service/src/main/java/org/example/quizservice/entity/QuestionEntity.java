package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizservice.enums.QuestionType;

@Entity(name = "question")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "text", columnDefinition = "LONGTEXT")
    String text;

    @Column(name = "points")
    Integer points;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    @Column(name = "author")
    String userId;

    @ManyToOne
    @JoinColumn(name = "question_bank_id")
    QuestionBankEntity questionBank;
}