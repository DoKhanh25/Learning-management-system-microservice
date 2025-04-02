package org.example.quizservice.repository;

import org.example.quizservice.entity.MultipleChoiceOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceOptionRepository extends JpaRepository<MultipleChoiceOptionEntity, Long> {

}
