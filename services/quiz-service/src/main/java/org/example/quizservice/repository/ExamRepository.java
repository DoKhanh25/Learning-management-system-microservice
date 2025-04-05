package org.example.quizservice.repository;

import org.example.quizservice.entity.ExamEntity;
import org.example.quizservice.enums.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    List<ExamEntity> findByCourseId(Long courseId);
    
    @Query("SELECT e FROM exam e WHERE e.courseId = :courseId AND e.startTime <= :now AND e.endTime >= :now")
    List<ExamEntity> findAvailableExamsByCourse(Long courseId, Date now);
    
    @Query("SELECT e FROM exam e WHERE e.courseId IN :courseIds AND e.startTime <= :now AND e.endTime >= :now")
    List<ExamEntity> findAvailableExamsByCoursesIn(List<Long> courseIds, Date now);
}
