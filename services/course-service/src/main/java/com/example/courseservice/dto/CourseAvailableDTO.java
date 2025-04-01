package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseAvailableDTO {
    Long id;
    String name;
    String summary;
    boolean isAttended;
    Date startDate;
    Date endDate;
}
