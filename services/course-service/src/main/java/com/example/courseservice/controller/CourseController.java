package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseCreateDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class CourseController {
    @Autowired
    CourseService courseService;
    @GetMapping("/getAllCourses")
    public ResponseEntity<ResultDTO> getAllCourses() {
        return courseService.getAllCourses();
    }
    @PostMapping("/addCourse")
    public ResponseEntity<ResultDTO> addCourse(@RequestBody CourseCreateDTO courseCreateDTO){
        return courseService.addCourse(courseCreateDTO);
    }

    @GetMapping("/getCourseById")
    public ResponseEntity<ResultDTO> getCourseById(@RequestParam Long id){
        log.info("getCourseById: " + id);
        return courseService.getCourseById(id);
    }
}
