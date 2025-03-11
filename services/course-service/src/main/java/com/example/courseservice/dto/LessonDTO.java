package com.example.courseservice.dto;

import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.entity.LessonTimerEntity;
import jakarta.persistence.*;
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
    List<LessonPagesEntity> lessonPages;
    List<LessonTimerEntity> lessonTimers;
    Date createdTime;
    Date updatedTime;
    CourseSectionsDTO section;
    Long sectionId;
}
