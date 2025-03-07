package com.example.courseservice.repository;

import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.enums.EnrolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrolRepository extends JpaRepository<EnrolEntity, Long> {

    @Query("SELECT e from enrol e where e.course.id = :id and e.enrolType = :enrolType")
    List<EnrolEntity> getEnrolEntitiesByCourseIdAndEnrolType(@Param("id") Long id,
                                                             @Param("enrolType") EnrolType enrolType);
}
