package com.example.courseservice.dto;

import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import lombok.Data;

import java.util.Date;
@Data
public class EnrolCreateDTO {
    Long course;
    String name;
    short status;
    EnrolType enrolType;
    String courseRole;
    String password;
    Date enrolStartDate;
    Date enrolEndDate;
}
