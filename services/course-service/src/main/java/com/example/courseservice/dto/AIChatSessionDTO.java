package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatSessionDTO {
    Long id;
    String userId;
    List<AIChatDTO> messages;
    String contextUsed;
    String sessionName;
    Date createdTime;
    Date updatedTime;
    LessonDTO lesson;
    Long lessonId;
}
