package org.example.quizservice.repository;

import org.example.quizservice.entity.QuestionBankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBankEntity, Long> {

    List<QuestionBankEntity> findByCourseId(Long courseId);
}
