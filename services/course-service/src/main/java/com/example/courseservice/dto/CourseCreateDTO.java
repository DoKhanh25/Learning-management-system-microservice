package com.example.courseservice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CourseCreateDTO {
    String name;
    String summary;
    short showGrades;
    Date startDate;
    Date endDate;
}
