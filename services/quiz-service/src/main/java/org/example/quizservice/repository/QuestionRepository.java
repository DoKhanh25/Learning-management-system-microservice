package org.example.quizservice.repository;

import org.example.quizservice.entity.QuestionEntity;
import org.example.quizservice.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    @Query("select qe from question qe where qe.questionType =:questionType and qe.questionBank.id =:questionBankId")
    List<QuestionEntity> findQuestionEntitiesByQuestionTypeAndQuestionBankId(QuestionType questionType, Long questionBankId);

    @Query("select qe from question qe where qe.questionBank.id =:questionBankId")
    List<QuestionEntity> findQuestionEntitiesByQuestionBankId(Long questionBankId);
}
