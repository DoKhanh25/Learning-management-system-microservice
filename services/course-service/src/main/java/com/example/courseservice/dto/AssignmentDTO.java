package com.example.courseservice.dto;

import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentDTO {
    Long id;
    String name;
    CourseEntity course;
    String description;
    Long courseId;
    List<AssignmentSubmissionsDTO> assignmentSubmissions;
    AssignmentType assignmentType;
    Boolean resubmit;
    Boolean preventLate;
    Date startDate;
    Date endDate;
}
