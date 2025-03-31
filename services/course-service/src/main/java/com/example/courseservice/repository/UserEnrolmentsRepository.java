package com.example.courseservice.repository;

import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserEnrolmentsRepository extends JpaRepository<UserEnrolmentsEntity, Long> {
    @Query("select u from user_enrolment u where u.enrol.course.id = :id")
    List<UserEnrolmentsEntity> getAllUserEnrolmentsByCourseId(@Param("id") Long courseId);

    @Query("select u.enrol.courseRole from user_enrolment u where u.userId = :userId")
    CourseRole getCourseRoleByUserId(@Param("userId") String userId);

    @Query("select u.enrol from user_enrolment u where u.userId = :userId and u.enrol.course.id = :courseId")
    EnrolEntity getEnrolEntityByUserId(@Param("userId") String userId,
                                       @Param("courseId") Long courseId);


    @Query("select u from user_enrolment u where u.enrol.course.id = :id and u.enrol.courseRole = com.example.courseservice.enums.CourseRole.STUDENT")
    List<UserEnrolmentsEntity> getAllStudentEnrolmentsByCourseId(@Param("id") Long courseId);

}
