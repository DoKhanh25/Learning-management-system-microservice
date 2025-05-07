package com.example.courseservice.controller;


import com.example.courseservice.dto.LessonBranchDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.LessonBranchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class LessonBranchController {
    @Autowired
    LessonBranchService lessonBranchService;

    @GetMapping("/getStudentProgressByLessonIdAndUserId")
    public ResponseEntity<ResultDTO> getStudentProgressByLessonIdAndUserId(@RequestParam("lessonId") Long lessonId,
                                                                           @RequestParam("userId") String userId,
                                                                           @RequestHeader("X-User-Id") String validateUserId){
        return lessonBranchService.getStudentProgressByLessonIdAndUserId(lessonId, userId, validateUserId);
    }

    @PostMapping("/saveLessonBranch")
    public ResponseEntity<ResultDTO> saveLessonBranch(@RequestBody LessonBranchDTO lessonBranchDTO,
                                                      @RequestHeader("X-User-Id") String userId){
        return lessonBranchService.saveLessonBranch(lessonBranchDTO, userId);
    }

    @GetMapping("/getStudentProgressByLessonId")
    public ResponseEntity<ResultDTO> getStudentProgressByLessonId(@RequestParam("lessonId") Long lessonId,
                                                                           @RequestHeader("X-User-Id") String validateUserId){
        return lessonBranchService.getStudentProgressByLessonId(lessonId, validateUserId);
    }
}
