package org.example.quizservice.repository;

import org.example.quizservice.entity.MultipleChoiceSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceSubmissionRepository extends JpaRepository<MultipleChoiceSubmissionEntity, Long> {

}
