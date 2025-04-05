package org.example.quizservice.repository;

import org.example.quizservice.entity.ExamQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestionEntity, Long> {
    List<ExamQuestionEntity> findByExamId(Long examId);

    @Query("SELECT eq from exam_question eq where eq.question.id = :questionId and eq.exam.id = :examId")
    ExamQuestionEntity findByQuestionIdAndExamId(Long questionId, Long examId);
    
    @Query("SELECT COUNT(eq) FROM exam_question eq WHERE eq.exam.id = :examId")
    Integer countQuestionsByExamId(Long examId);

    @Query("SELECT COUNT(eq) FROM exam_question eq WHERE eq.exam.id = :examId AND eq.question.questionBank.id = :questionBankId")
    Integer countQuestionsByExamIdAndQuestionBankId(Long examId, Long questionBankId);

    Optional<ExamQuestionEntity> findByExamIdAndQuestionId(Long examId, Long questionId);
}
