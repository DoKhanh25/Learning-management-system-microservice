package com.example.courseservice.repository;

import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrolRepository extends JpaRepository<EnrolEntity, Long> {

    @Query("SELECT e from enrol e where e.course.id = :id and e.enrolType = :enrolType")
    List<EnrolEntity> getEnrolEntitiesByCourseIdAndEnrolType(@Param("id") Long id, @Param("enrolType") EnrolType enrolType);

    @Query("select e from enrol e where e.course.id = :courseId and e.password = :password and e.courseRole = :courseRole")
    EnrolEntity getEnrolEntitiesByCourseIdAndPasswordAndEnrolType(@Param("courseId") Long courseId,
                                                                  @Param("password") String password,
                                                                  @Param("courseRole") CourseRole courseRole);

    @Query("select e.password from enrol e where e.course.id =:courseId")
    List<String> findAllPasswordByCourseId(Long courseId);
}
