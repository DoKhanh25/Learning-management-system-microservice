package com.example.courseservice.controller;

import com.example.courseservice.dto.EnrolDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.UserEnrolmentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserEnrolmentsController {
    @Autowired
    UserEnrolmentsService userEnrolmentsService;

    @GetMapping("/getAllUserEnrolmentsByCourseId")
    public ResponseEntity<ResultDTO> getAllUserEnrolmentsByCourseId(@RequestParam Long id){
        return userEnrolmentsService.getAllUserEnrolmentsByCourseId(id);
    }

    @PostMapping("/addSelfUserEnrolment")
    public ResponseEntity<ResultDTO> addSelfUserEnrolment(@RequestBody EnrolDTO enrolDTO,
                                                          @RequestHeader("X-User-Id") String userId){
        return userEnrolmentsService.addSelfUserEnrolment(enrolDTO, userId);
    }

    @GetMapping("/getAllStudentEnrolmentsByCourseId")
    public ResponseEntity<ResultDTO> getAllStudentEnrolmentsByCourseId(@RequestParam Long id){
        return userEnrolmentsService.getAllStudentEnrolmentsByCourseId(id);
    }

    @DeleteMapping("/deleteUserEnrolmentsByUserIdAndCourseId")
    public ResponseEntity<ResultDTO> deleteUserEnrolmentsByUserIdAndCourseId(@RequestParam Long courseId,
                                                                             @RequestParam String userId){
        return userEnrolmentsService.deleteUserEnrolmentsByUserIdAndCourseId(courseId, userId);
    }
}
