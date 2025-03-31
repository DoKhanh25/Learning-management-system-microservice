package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.CourseSectionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class CourseSectionsController {
    @Autowired
    CourseSectionsService courseSectionsService;

    @PostMapping("/addCourseSection")
    public ResponseEntity<ResultDTO> addCourseSection(@RequestBody CourseSectionsDTO courseSectionsDTO,
                                                      @RequestHeader("X-User-Id") String userId){
            return courseSectionsService.addCourseSection(courseSectionsDTO, userId);
    }

    @PutMapping("/updateCourseSection")
    public ResponseEntity<ResultDTO> updateCourseSection(@RequestBody CourseSectionsDTO courseSectionsDTO,
                                                         @RequestHeader("X-User-Id") String userId){
        return courseSectionsService.updateCourseSection(courseSectionsDTO, userId);
    }

    @DeleteMapping("/deleteCourseSectionById")
    public ResponseEntity<ResultDTO> deleteCourseSectionById(@RequestParam Long id,
                                                             @RequestHeader("X-User-Id") String userId){
        return courseSectionsService.deleteCourseSectionById(id, userId);
    }

}
