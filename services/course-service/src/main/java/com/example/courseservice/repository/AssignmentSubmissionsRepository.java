package com.example.courseservice.repository;

import com.example.courseservice.entity.AssignmentSubmissionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssignmentSubmissionsRepository extends JpaRepository<AssignmentSubmissionsEntity, Long> {


    @Query("select asb from assignment_submissions asb where asb.userId = ?1 and asb.assignment.id = ?2")
    AssignmentSubmissionsEntity findByUserIdAndAssignmentId(String userId, Long assignmentId);

    @Query("select asb from assignment_submissions asb where asb.assignment.id = ?1")
    List<AssignmentSubmissionsEntity> findAllByAssignmentId(Long assignmentId);
}
