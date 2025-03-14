package com.example.courseservice.controller;

import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.LessonPagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
@Slf4j
public class LessonPagesController {

    @Autowired
    LessonPagesService lessonPagesService;

    @GetMapping("/getLessonPagesByLessonId")
    public ResponseEntity<ResultDTO> getLessonPagesByLessonId(@RequestParam Long lessonId){
        return lessonPagesService.getLessonPagesByLessonId(lessonId);
    }

    @GetMapping("/getDocumentFileByLessonPagesId")
    public ResponseEntity<Resource> getDocumentFileByLessonPagesId(@RequestParam Long id){
        return lessonPagesService.getDocumentFileByLessonPagesId(id);
    }

    @GetMapping("/getVideoFileLessonPagesById")
    public ResponseEntity<StreamingResponseBody> getVideoFileByLessonPagesId(@RequestParam Long id,
                                                                             @RequestHeader(value = "Range", required = false) String rangeHeader){
        return lessonPagesService.getVideoFileLessonPagesById(id, rangeHeader);
    }

    @PostMapping("/addContentLessonPage")
    public ResponseEntity<ResultDTO> addLessonPage(@RequestBody LessonPagesDTO lessonPagesDTO){
        return lessonPagesService.addContentLessonPage(lessonPagesDTO);
    }

    @PostMapping("/addDocumentLessonPage")
    public ResponseEntity<ResultDTO> addDocumentLessonPage(@ModelAttribute LessonPagesDTO lessonPagesDTO,
                                                           @RequestParam("file") MultipartFile file){
        return lessonPagesService.addDocumentLessonPage(lessonPagesDTO, file);
    }

    @PostMapping("/addUploadVideoLessonPage")
    public ResponseEntity<ResultDTO> addUploadVideoLessonPage(@ModelAttribute LessonPagesDTO lessonPagesDTO,
                                                        @RequestParam("file") MultipartFile file){
        return lessonPagesService.addUploadVideoLessonPage(lessonPagesDTO, file);
    }

    @PostMapping("/addVideoURLLessonPage")
    public ResponseEntity<ResultDTO> addVideoURLLessonPage(@RequestBody LessonPagesDTO lessonPagesDTO){
        return lessonPagesService.addVideoURLLessonPage(lessonPagesDTO);
    }

    @DeleteMapping("/deleteLessonPageById")
    public ResponseEntity<ResultDTO> deleteLessonPageById(@RequestParam Long id){
        return lessonPagesService.deleteLessonPageById(id);
    }
}
