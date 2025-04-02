package org.example.quizservice.repository;

import org.example.quizservice.entity.MultipleChoiceQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceQuestionRepository extends JpaRepository<MultipleChoiceQuestionEntity, Long> {
    @Query("select mq from multiple_choice_question mq where mq.questionBank.id =:id")
    List<MultipleChoiceQuestionEntity> findMultipleChoiceQuestionsByQuestionBankId(Long id);


}
