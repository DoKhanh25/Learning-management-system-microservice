package com.example.courseservice.controller;

import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.EnrolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class EnrolController {
    @Autowired
    EnrolService enrolService;

    @GetMapping("/getEnrolByCourseAndEnrolType")
    public ResponseEntity<ResultDTO> getEnrolByCourseAndEnrolTypeManual(@RequestParam Long id, @RequestParam String enrolType){
        return enrolService.getEnrolByCourseAndEnrolTypeManual(id, enrolType);
    }
}
