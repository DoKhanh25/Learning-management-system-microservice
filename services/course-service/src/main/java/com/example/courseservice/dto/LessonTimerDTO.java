package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonTimerDTO {
    Long id;
    String userId;
    Date startTime;
    Long lessonTime;
    short completed;
    LessonDTO lesson;
    Long lessonId;
}
