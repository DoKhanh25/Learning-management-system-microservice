package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    @Query("SELECT l FROM lesson l WHERE l.section.id = :sectionId")
    List<LessonEntity> findLessonEntitiesBySectionId(Long sectionId);

    @Query("SELECT l FROM lesson l WHERE l.section.course.id = :courseId")
    List<LessonEntity> findLessonEntitiesByCourseId(Long courseId);

    @Query("select l.lessonPages from lesson l where l.id = :lessonId")
    List<LessonPagesEntity> findLessonPagesEntitiesByLessonId(@Param("lessonId") Long lessonId);

}
