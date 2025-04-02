package org.example.quizservice.repository;

import org.example.quizservice.entity.EssayQuestionEntity;
import org.example.quizservice.entity.MultipleChoiceQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EssayQuestionRepository extends JpaRepository<EssayQuestionEntity, Long> {
    @Query("select eq from essay_question eq where eq.questionBank.id =:id")
    List<EssayQuestionEntity> findEssayQuestionEntitiesByQuestionBankId(Long id);
}
