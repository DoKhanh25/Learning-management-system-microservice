package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    @Query("SELECT l FROM lesson l WHERE l.section.id = :sectionId")
    List<LessonEntity> findLessonEntitiesBySectionId(Long sectionId);

    @Query("SELECT l FROM lesson l WHERE l.section.course.id = :courseId")
    List<LessonEntity> findLessonEntitiesByCourseId(Long courseId);

}
