package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonTimerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonTimerRepository extends JpaRepository<LessonTimerEntity, Long> {
    @Query("select l from lesson_timer l where l.lesson.id =:lessonId")
    List<LessonTimerEntity> findLessonTimerEntitiesByLessonId(@Param("lessonId") Long lessonId);

    @Query("select l from lesson_timer l where l.lesson.id =:lessonId and l.userId =:userId")
    LessonTimerEntity findLessonTimerEntityByLessonIdAndUserId(@Param("lessonId") Long lessonId,
                                                               @Param("userId") String userId);
}
