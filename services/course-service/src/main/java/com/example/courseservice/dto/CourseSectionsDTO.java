package com.example.courseservice.dto;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSectionsDTO {
    Long id;
    Long courseId;
    CourseDTO course;
    List<LessonDTO> lessons;
    String name;
    String summary;
    Long section;
    Date createdTime;
    Date updatedTime;
}
