package com.example.courseservice.controller;

import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.UserEnrolmentsService;
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
public class UserEnrolmentsController {
    @Autowired
    UserEnrolmentsService userEnrolmentsService;
    @GetMapping("/getAllUserEnrolmentsByCourseId")
    public ResponseEntity<ResultDTO> getAllUserEnrolmentsByCourseId(@RequestParam Long id){
        return userEnrolmentsService.getAllUserEnrolmentsByCourseId(id);
    }
}
