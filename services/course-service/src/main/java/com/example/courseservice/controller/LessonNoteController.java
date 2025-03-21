package com.example.courseservice.controller;


import com.example.courseservice.dto.LessonNoteDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.LessonNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class LessonNoteController {
    @Autowired
    LessonNoteService lessonNoteService;

    @GetMapping("/getAllLessonNotesByLessonPageId")
    public ResponseEntity<ResultDTO> getAllLessonNotesByLessonPageId(@RequestParam Long lessonPagesId,
                                                                     @RequestHeader("X-User-Id") String userId,
                                                                     @RequestHeader("X-Roles") String roles
                                                                     ){
        return lessonNoteService.getAllLessonNotesByLessonPageId(lessonPagesId, userId, roles);
    }

    @GetMapping("/getLessonNoteByLessonPageIdAndUserId")
    public ResponseEntity<ResultDTO> getLessonNoteByLessonPageIdAndUserId(@RequestParam Long lessonPagesId,
                                                                          @RequestHeader("X-User-Id") String userId){
        return lessonNoteService.getLessonNoteByLessonPageIdAndUserId(lessonPagesId, userId);
    }

    @PostMapping("/saveLessonNote")
    public ResponseEntity<ResultDTO> saveLessonNote(@RequestBody LessonNoteDTO lessonNoteDTO,
                                                   @RequestHeader("X-User-Id") String userId) {
        return lessonNoteService.saveLessonNote(lessonNoteDTO, userId);
    }
    @DeleteMapping("/deleteLessonNote")
    public ResponseEntity<ResultDTO> deleteLessonNote(@RequestParam Long lessonNoteId,
                                                      @RequestHeader("X-User-Id") String userId){
        return lessonNoteService.deleteLessonNote(lessonNoteId, userId);
    }
}
