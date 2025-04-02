package org.example.quizservice.repository;

import org.example.quizservice.entity.CodingQuestionEntity;
import org.example.quizservice.entity.EssayQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingQuestionRepository extends JpaRepository<CodingQuestionEntity, Long> {
    @Query("select cq from coding_question cq where cq.questionBank.id =:id")
    List<CodingQuestionEntity> findCodingQuestionEntitiesByQuestionBankId(Long id);
}