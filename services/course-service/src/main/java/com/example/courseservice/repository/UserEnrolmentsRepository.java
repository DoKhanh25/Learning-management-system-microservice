package com.example.courseservice.repository;

import com.example.courseservice.entity.CourseEntity;
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

    @Query("select c from course c " +
            "join enrol e on c.id = e.course.id " +
            "join user_enrolment ue on e.id = ue.enrol.id " +
            "where ue.userId = :userId")
    List<CourseEntity> getCoursesByUserId(@Param("userId") String userId);

    @Query("select ue from user_enrolment ue where ue.enrol.course.id =:courseId")
    List<UserEnrolmentsEntity> getUserEnrolmentsByCourseId(@Param("courseId") Long courseId);
}
