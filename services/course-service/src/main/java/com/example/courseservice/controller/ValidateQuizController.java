package com.example.courseservice.controller;

import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validateQuiz")
public class ValidateQuizController {

    @Autowired
    private UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/validateIsTeacherInCourse")
    public ResultDTO validateIsTeacherInCourse(@RequestParam("courseId") Long courseId,
                                               @RequestParam("userId") String userId) {
        ResultDTO resultDTO = new ResultDTO();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission");
            return resultDTO;
        }

        if(enrolEntity.getCourseRole() == CourseRole.STUDENT){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission");
            return resultDTO;
        }

        resultDTO.setStatus(1);
        resultDTO.setMessage("you have permission");
        return resultDTO;
    }

    @GetMapping("/validateIsInCourse")
    public ResultDTO validateIsCourse(@RequestParam("courseId") Long courseId,
                                               @RequestParam("userId") String userId) {
        ResultDTO resultDTO = new ResultDTO();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission");
            return resultDTO;
        }


        resultDTO.setStatus(1);
        resultDTO.setMessage("you have permission");
        return resultDTO;
    }
}
