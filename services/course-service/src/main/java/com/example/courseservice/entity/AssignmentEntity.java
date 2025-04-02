package com.example.courseservice.entity;


import com.example.courseservice.enums.AssignmentType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity(name = "assignment")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    CourseEntity course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    List<AssignmentSubmissionsEntity> assignmentSubmissions;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    String description;

    @Column(name = "assignment_type")
    AssignmentType assignmentType;

    @Column(name = "resubmit")
    Boolean resubmit;

    @Column(name = "prevent_late")
    Boolean preventLate;

    @Column(name = "start_date")
    Date startDate;

    @Column(name = "end_date")
    Date endDate;

}
