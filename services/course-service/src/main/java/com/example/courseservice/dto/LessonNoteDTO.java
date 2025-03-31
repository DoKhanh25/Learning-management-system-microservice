package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonNoteDTO {
    Long id;
    String userId;
    String note;
    Date createdTime;
    Date updatedTime;
    Long lessonPagesId;
    LessonPagesDTO lessonPages;
}
