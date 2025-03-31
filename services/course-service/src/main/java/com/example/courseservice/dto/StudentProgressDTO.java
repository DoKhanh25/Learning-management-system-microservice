package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentProgressDTO {
    Long lessonBranchId;
    Long lessonId;
    Long lessonPagesId;
    String title;
    String userId;
    short completed;
    Date startTime;
}
