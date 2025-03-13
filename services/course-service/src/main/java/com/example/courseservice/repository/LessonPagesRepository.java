package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonPagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonPagesRepository extends JpaRepository<LessonPagesEntity, Long> {
}
