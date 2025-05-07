package org.example.quizservice.repository;

import org.example.quizservice.entity.QuestionSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionSubmissionRepository extends JpaRepository<QuestionSubmissionEntity, Long> {
    List<QuestionSubmissionEntity> findByExamSubmissionId(Long examSubmissionId);
    
    @Query("SELECT qs FROM question_submission qs WHERE qs.examSubmission.id = :examSubmissionId AND qs.question.id = :questionId")
    Optional<QuestionSubmissionEntity> findByExamSubmissionIdAndQuestionId(Long examSubmissionId, Long questionId);



}
