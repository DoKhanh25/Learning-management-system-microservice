package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity(name = "assignment_submissions")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AssignmentSubmissionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "userId", nullable = false)
    String userId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    AssignmentEntity assignment;

    @Column(name = "numfiles")
    Long numfiles;

    @Column(name = "data1", columnDefinition = "LONGTEXT")
    String data1;

    @Column(name = "data2", columnDefinition = "LONGTEXT")
    String data2;

    @Column(name = "grade")
    Long grade;

    @Column(name = "submission_comment", columnDefinition = "LONGTEXT")
    String submissionComment;

    @Column(name = "updatedTime")
    Date updatedTime;

    @Column(name = "createdTime")
    Date createdTime;
}
