package com.example.courseservice.repository;

import com.example.courseservice.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    @Query("SELECT am from assignment am where am.course.id =:courseId")
    public List<AssignmentEntity> findAssignmentEntitiesByCourseId(@Param("courseId") Long courseId);
}
