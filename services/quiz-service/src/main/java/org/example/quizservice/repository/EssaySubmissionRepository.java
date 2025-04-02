package org.example.quizservice.repository;

import org.example.quizservice.entity.EssaySubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EssaySubmissionRepository extends JpaRepository<EssaySubmissionEntity, Long> {

}
