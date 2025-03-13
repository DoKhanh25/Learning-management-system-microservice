package com.example.courseservice.dto;

import com.example.courseservice.entity.LessonPagesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDTO {
    Long id;
    String name;
    String intro;
    List<LessonPagesDTO> lessonPages;
    List<LessonTimerDTO> lessonTimers;
    Date createdTime;
    Date updatedTime;
    CourseSectionsDTO section;
    Long sectionId;
}
