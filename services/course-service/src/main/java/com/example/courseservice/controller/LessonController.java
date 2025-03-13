package com.example.courseservice.controller;


import com.example.courseservice.dto.LessonDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.LessonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class LessonController {
    @Autowired
    LessonService lessonService;

    @GetMapping("/findLessonEntitiesBySectionId")
    public ResponseEntity<ResultDTO> findLessonEntitiesBySectionId(@RequestParam Long sectionId){
        return lessonService.findLessonEntitiesBySectionId(sectionId);
    }

    @GetMapping("/findLessonEntitiesByCourseId")
    public ResponseEntity<ResultDTO> findLessonEntitiesByCourseId(@RequestParam Long courseId){
        return lessonService.findLessonEntitiesByCourseId(courseId);
    }

    @GetMapping("/getLessonById")
    public ResponseEntity<ResultDTO> getLessonById(@RequestParam Long id){
        return lessonService.getLessonById(id);
    }

    @PostMapping("/addLesson")
    public ResponseEntity<ResultDTO> addLesson(@RequestBody LessonDTO lessonDTO){
        return lessonService.addLesson(lessonDTO);
    }
}
