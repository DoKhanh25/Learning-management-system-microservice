package com.example.courseservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonBranchDTO {
    Long id;
    LessonPagesDTO lessonPages;
    Long lessonId;
    String userId;
    Long timeSeen;

}
