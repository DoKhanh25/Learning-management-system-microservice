package org.example.quizservice.repository;

import org.example.quizservice.entity.ExamEntity;
import org.example.quizservice.enums.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    List<ExamEntity> findByCourseId(Long courseId);
}
