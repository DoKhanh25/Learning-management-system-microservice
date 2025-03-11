package com.example.courseservice.repository;

import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.entity.CourseSectionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseSectionsRepository extends JpaRepository<CourseSectionsEntity, Long> {

    @Query("SELECT c FROM course_sections c WHERE c.course.id = :courseId")
    List<CourseSectionsEntity> getAllCourseSectionsByCourseId(Long courseId);
}
