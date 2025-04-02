package com.example.courseservice.controller;

import com.example.courseservice.dto.EnrolDTO;
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
    public ResponseEntity<ResultDTO> createSelfEnrol(@RequestBody EnrolDTO enrolDTO){
        return enrolService.createSelfEnrol(enrolDTO);
    }

    @PostMapping("/addEnrolmentsByCohort")
    public ResponseEntity<ResultDTO> addEnrolmentsByCohort(@RequestParam("cohortId") Long cohortId,
                                                           @RequestParam("courseId") Long courseId,
                                                           @RequestParam("courseRole") String courseRole){
        return enrolService.addEnrolmentsByCohort(cohortId, courseId, courseRole);
    }

    @PostMapping("/addUserEnrolment")
    public ResponseEntity<ResultDTO> addUserEnrolment(@RequestBody EnrolDTO enrolDTO,
                                                           @RequestParam("userId") String userId){
        return enrolService.addUserEnrolment(enrolDTO, userId);
    }


}
