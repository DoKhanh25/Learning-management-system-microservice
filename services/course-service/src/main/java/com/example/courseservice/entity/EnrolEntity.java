package com.example.courseservice.entity;

import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "enrol")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EnrolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    CourseEntity course;

    @Column(name = "name")
    String name;

    @Column(name = "status")
    short status;

    @OneToMany(mappedBy = "enrol", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<UserEnrolmentsEntity> userEnrolments;


    @Column(name = "enrol_type")
    EnrolType enrolType;

    @Column(name = "course_role")
    CourseRole courseRole;

    @Column(name = "password")
    String password;

    @Column(name = "enrol_start_date")
    Date enrolStartDate;

    @Column(name = "enrol_end_date")
    Date enrolEndDate;

}
