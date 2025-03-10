package com.example.courseservice.dto;

import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrolDTO {
    Long course;
    String name;
    short status;
    List<UserEnrolmentsDTO> userEnrolments;
    CourseRole courseRole;
    EnrolType enrolType;
    String password;
    Date enrolStartDate;
    Date enrolEndDate;
}
