package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonAttemptsDTO {
    Long id;
    LessonPagesDTO lessonPages;
    Long lessonPagesId;
    Long lessonId;
    String userId;
    Long timeSeen;
    String correct;
    String userAnswer;
}
