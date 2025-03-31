package com.example.courseservice.controller;

import com.example.courseservice.dto.LessonTimerDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.LessonTimerEntity;
import com.example.courseservice.services.LessonTimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class LessonTimerController {

    @Autowired
    private LessonTimerService lessonTimerService;

    @GetMapping("/getAllLessonTimers")
    public ResponseEntity<ResultDTO> getAllLessonTimers(@RequestParam("lessonId") Long lessonId,
                                                        @RequestHeader("X-User-Id") String userId){
        return lessonTimerService.getAllLessonTimers(lessonId, userId);
    }


    @PostMapping("/saveLessonTimer")
    public ResponseEntity<ResultDTO> saveLessonTimer(@RequestBody LessonTimerDTO lessonTimerDTO,
                                                     @RequestHeader("X-User-Id") String userId) {
        return lessonTimerService.saveLessonTimer(lessonTimerDTO, userId);
    }
}
