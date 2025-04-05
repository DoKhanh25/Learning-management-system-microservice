package org.example.quizservice.repository;

import org.example.quizservice.entity.MultipleChoiceOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceOptionRepository extends JpaRepository<MultipleChoiceOptionEntity, Long> {
    @Query("SELECT mo from multiple_choice_option mo where mo.question.id =:questionId")
    List<MultipleChoiceOptionEntity> findByQuestionId(Long questionId);
}
