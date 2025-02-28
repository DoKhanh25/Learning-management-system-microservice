package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Entity(name = "user_enrolment")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserEnrolmentsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "enrol_id", nullable = false)
    private EnrolEntity enrol;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_end")
    private Date timeEnd;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "updated_time")
    Date updatedTime;
}
