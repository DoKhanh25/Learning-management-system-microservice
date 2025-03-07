package com.example.courseservice.controller;

import com.example.courseservice.dto.EnrolCreateDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.EnrolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class EnrolController {
    @Autowired
    EnrolService enrolService;

    @GetMapping("/getEnrolByCourseAndEnrolType")
    public ResponseEntity<ResultDTO> getEnrolByCourseAndEnrolTypeManual(@RequestParam Long id, @RequestParam String enrolType){
        return enrolService.getEnrolByCourseAndEnrolType(id, enrolType);
    }

    @PostMapping("/createSelfEnrol")
    public ResponseEntity<ResultDTO> createSelfEnrol(@RequestBody EnrolCreateDTO enrolCreateDTO){
        return enrolService.createSelfEnrol(enrolCreateDTO);
    }

    @PostMapping("/addEnrolmentsByCohort")
    public ResponseEntity<ResultDTO> addEnrolmentsByCohort(@RequestParam("cohortId") Long cohortId,
                                                           @RequestParam("courseId") Long courseId,
                                                           @RequestParam("courseRole") String courseRole){
        return enrolService.addEnrolmentsByCohort(cohortId, courseId, courseRole);
    }
}
