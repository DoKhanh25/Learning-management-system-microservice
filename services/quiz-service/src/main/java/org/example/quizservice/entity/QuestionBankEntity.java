package org.example.quizservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity(name = "question_bank")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class QuestionBankEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    String description;

    @Column(name = "course_id", nullable = false)
    Long courseId;

    @Column(name = "author")
    String userId;

    @OneToMany(mappedBy = "questionBank", cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH,
            CascadeType.DETACH
    }, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    List<QuestionEntity> questions;
}
