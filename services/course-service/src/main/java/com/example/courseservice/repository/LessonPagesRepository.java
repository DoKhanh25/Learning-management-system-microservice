package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonPagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonPagesRepository extends JpaRepository<LessonPagesEntity, Long> {

    @Query("SELECT lp FROM lesson_pages lp WHERE lp.lesson.id = :lessonId")
    List<LessonPagesEntity> findLessonPagesEntitiesByLessonId(@Param("lessonId") Long lessonId);
}
