package com.example.courseservice.repository;

import com.example.courseservice.entity.UserEnrolmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserEnrolmentsRepository extends JpaRepository<UserEnrolmentsEntity, Long> {
    @Query("select u from user_enrolment u where u.enrol.course.id = :id")
    List<UserEnrolmentsEntity> getAllUserEnrolmentsByCourseId(@Param("id") Long courseId);


}
