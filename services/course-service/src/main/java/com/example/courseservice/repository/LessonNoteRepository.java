package com.example.courseservice.repository;

import com.example.courseservice.entity.LessonNoteEntity;
import com.example.courseservice.enums.CourseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonNoteRepository extends JpaRepository<LessonNoteEntity, Long> {

    @Query("SELECT ln from lesson_note ln where ln.lessonPages.id = :id")
    List<LessonNoteEntity> getAllLessonNotesByLessonPageId(@Param("id") Long id);

    @Query("SELECT e.courseRole from enrol e " +
            "LEFT JOIN course c ON e.course.id = c.id " +
            "LEFT JOIN course_sections cs ON cs.course.id = c.id " +
            "LEFT JOIN lesson l ON cs.id = l.section.id " +
            "LEFT JOIN lesson_pages lp ON lp.lesson.id = l.id " +
            "LEFT JOIN user_enrolment ue ON e.id = ue.id " +
            "WHERE ue.userId = :userId AND lp.id = :lessonPagesId")
    CourseRole findCourseRoleByUserIdAndLessonPagesId(@Param("userId") String userId, @Param("lessonPagesId") Long lessonPagesId);

    @Query("select ln from lesson_note ln where ln.userId = :userId and ln.lessonPages.id = :lessonPagesId ")
    LessonNoteEntity getLessonNoteByLessonPageIdAndUserId(@Param("userId") String userId, @Param("lessonPagesId") Long lessonPagesId);

    @Query("select ln from lesson_note ln where ln.userId = :userId and ln.id = :lessonNoteId ")
    LessonNoteEntity getLessonNoteByUserId(@Param("userId") String userId, @Param("lessonNoteId") Long lessonNoteId);
}
