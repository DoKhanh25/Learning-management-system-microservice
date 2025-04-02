package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseCreateDTO;
import com.example.courseservice.dto.CourseDTO;
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
    public ResponseEntity<ResultDTO> getAllCourses(){
        return courseService.getAllCourses();
    }

    @GetMapping("/getAllCourseAvailable")
    public ResponseEntity<ResultDTO> getAllCourseAvailable(@RequestHeader("X-User-Id") String userId){
        return courseService.getAllCourseAvailable(userId);
    }

    @PutMapping("/updateCourse")
    public ResponseEntity<ResultDTO> updateCourse(@RequestBody CourseCreateDTO courseDTO,
                                                  @RequestParam("id") Long courseId){
        return courseService.updateCourse(courseDTO, courseId);
    }


    @PostMapping("/addCourse")
    public ResponseEntity<ResultDTO> addCourse(@RequestBody CourseCreateDTO courseCreateDTO){
        return courseService.addCourse(courseCreateDTO);
    }

    @GetMapping("/getCourseById")
    public ResponseEntity<ResultDTO> getCourseById(@RequestParam Long id){
        return courseService.getCourseById(id);
    }

    @DeleteMapping("/deleteCourseById")
    public ResponseEntity<ResultDTO> deleteCourseById(@RequestParam Long id) {
        return courseService.deleteCourseById(id);
    }


    @GetMapping("/teacher/getAllCoursesByUserId")
    public ResponseEntity<ResultDTO> getAllTeacherCoursesByUserId(@RequestHeader("X-User-Id") String userId){
        return courseService.getAllTeacherCoursesByUserId(userId);
    }

    @GetMapping("/student/getAllCoursesByUserId")
    public ResponseEntity<ResultDTO> getAllStudentCoursesByUserId(@RequestHeader("X-User-Id") String userId){
        return courseService.getAllStudentCoursesByUserId(userId);
    }

    @GetMapping("/teacher/getCourseById")
    public ResponseEntity<ResultDTO> getTeacherCourseById(@RequestParam Long id){
        return courseService.getTeacherCourseById(id);
    }

}
