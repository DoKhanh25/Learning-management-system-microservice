package com.example.courseservice.dto;

import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.ResourceEntity;
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
    List<CourseSectionsEntity> courseSections;
    List<EnrolDTO> enrols;
//    List<ResourceEntity> resources;
    String name;
    String summary;
    short showGrades;
    Date startDate;
    Date endDate;
    Date createdTime;
    Date updatedTime;

}
