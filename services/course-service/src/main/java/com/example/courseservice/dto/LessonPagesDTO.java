package com.example.courseservice.dto;

import com.example.courseservice.enums.QType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonPagesDTO {
    Long id;
    LessonDTO lesson;
    Long lessonId;
    List<LessonAttemptsDTO> lessonAttempts;
    List<LessonBranchDTO> lessonBranch;
    Integer position;
    @JsonProperty("qType")
    String qType;
    String title;
    String content;
    Date createdTime;
    Date updatedTime;
}
