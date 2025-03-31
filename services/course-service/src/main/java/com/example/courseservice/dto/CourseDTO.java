package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    Long id;
    List<CourseSectionsDTO> courseSections;
    List<EnrolDTO> enrols;
    List<ResourceDTO> resources;
    String name;
    String summary;
    short showGrades;
    Date startDate;
    Date endDate;
    Date createdTime;
    Date updatedTime;

}
