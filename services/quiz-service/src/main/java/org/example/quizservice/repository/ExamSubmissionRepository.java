package org.example.quizservice.repository;

import org.example.quizservice.entity.ExamSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmissionEntity, Long> {
    List<ExamSubmissionEntity> findByUserId(String userId);
    
    Optional<ExamSubmissionEntity> findByUserIdAndExamId(String userId, Long examId);
    
    @Query("SELECT es FROM exam_submission es WHERE es.userId = :userId AND es.exam.id = :examId AND es.submissionTime IS NULL")
    Optional<ExamSubmissionEntity> findOngoingSubmission(String userId, Long examId);
    
    @Query("SELECT COUNT(es) FROM exam_submission es WHERE es.userId = :userId AND es.exam.id = :examId")
    Integer countSubmissionsByUserAndExam(String userId, Long examId);
}
