package com.example.courseservice.repository;

import com.example.courseservice.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    @Query("SELECT c FROM course c JOIN c.enrols e JOIN e.userEnrolments ue WHERE ue.userId = :userId AND e.courseRole = com.example.courseservice.enums.CourseRole.TEACHER")
    List<CourseEntity> getAllTeacherCoursesByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(c) FROM course c JOIN c.enrols e WHERE c.id = :courseId AND e.courseRole = com.example.courseservice.enums.CourseRole.STUDENT ")
    short countAllByCourseIdAndCourseRoleStudent(Long courseId);
}
