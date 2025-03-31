package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.mapper.CourseSectionsMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.CourseSectionsRepository;
import com.example.courseservice.repository.EnrolRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CourseSectionsService {

    @Autowired
    CourseSectionsRepository courseSectionsRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    private EnrolRepository enrolRepository;

    @Autowired
    private UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    CourseSectionsMapper courseSectionsMapper;

    public ResponseEntity<ResultDTO> getAllCourseSectionsByCourseId(Long courseId){
        ResultDTO resultDTO = new ResultDTO();
        List<CourseSectionsEntity> courseSectionsEntityList = courseSectionsRepository.getAllCourseSectionsByCourseId(courseId);
        resultDTO.setData(courseSectionsEntityList);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }


    public ResponseEntity<ResultDTO> addCourseSection(CourseSectionsDTO courseSectionsDTO,
                                                      String userId){
        ResultDTO resultDTO = new ResultDTO();

        if (courseSectionsDTO == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("CourseSectionsDTO cannot be null");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        if (courseSectionsDTO.getName() == null || courseSectionsDTO.getName().isBlank()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course section name cannot be null or blank");
            return ResponseEntity.badRequest().body(resultDTO);
        }
        CourseEntity courseEntity = courseRepository.findById(courseSectionsDTO.getCourseId()).orElse(null);
        if (courseEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course not found");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(courseEntity.getId());
        if (userEnrolmentsEntityList.isEmpty()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (!userEnrolmentsEntityList.stream().anyMatch(userEnrolmentsEntity -> userEnrolmentsEntity.getUserId().equals(userId))) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not enrolled in course");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        // validate role teacher can create Assignmet

        for (UserEnrolmentsEntity userEnrolments : userEnrolmentsEntityList) {
            if (userEnrolments.getUserId().equals(userId)) {
                if(userEnrolments.getEnrol().getCourseRole() == CourseRole.STUDENT){
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("User dont have permission to create Course Section");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }

        CourseSectionsEntity courseSectionsEntity = new CourseSectionsEntity();
        courseSectionsEntity.setName(courseSectionsDTO.getName());
        courseSectionsEntity.setSummary(courseSectionsDTO.getSummary());
        courseSectionsEntity.setSection(courseSectionsDTO.getSection());
        courseSectionsEntity.setCreatedTime(new Date());

        courseSectionsEntity.setCourse(courseEntity);

        CourseSectionsEntity courseSectionsEntityResult = courseSectionsRepository.save(courseSectionsEntity);
        resultDTO.setData(courseSectionsEntityResult);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateCourseSection(CourseSectionsDTO courseSectionsDTO, String userId){
        ResultDTO resultDTO = new ResultDTO();
        if (courseSectionsDTO == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("CourseSectionsDTO cannot be null");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        CourseSectionsEntity courseSectionsEntity = courseSectionsRepository.findById(courseSectionsDTO.getId()).orElse(null);
        if (courseSectionsEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course section not found");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        CourseEntity courseEntity = courseSectionsEntity.getCourse();

        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(courseEntity.getId());
        if (userEnrolmentsEntityList.isEmpty()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (!userEnrolmentsEntityList.stream().anyMatch(userEnrolmentsEntity -> userEnrolmentsEntity.getUserId().equals(userId))) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not enrolled in course");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        // validate role teacher can create Assignmet

        for (UserEnrolmentsEntity userEnrolments : userEnrolmentsEntityList) {
            if (userEnrolments.getUserId().equals(userId)) {
                if(userEnrolments.getEnrol().getCourseRole() == CourseRole.STUDENT){
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("User dont have permission to create Course Section");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }

        courseSectionsEntity.setSummary(courseSectionsDTO.getSummary());
        courseSectionsEntity = courseSectionsRepository.save(courseSectionsEntity);
        resultDTO.setData(courseSectionsEntity);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }


    public ResponseEntity<ResultDTO> deleteCourseSectionById(Long id, String userId){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseSectionsEntity> courseSectionsEntityOptional = courseSectionsRepository.findById(id);
        if(courseSectionsEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
        }

        CourseSectionsEntity courseSectionsEntity = courseSectionsEntityOptional.get();
        CourseEntity courseEntity = courseSectionsEntity.getCourse();

        if (courseEntity == null) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Course not found");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(courseEntity.getId());
        if (userEnrolmentsEntityList.isEmpty()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (!userEnrolmentsEntityList.stream().anyMatch(userEnrolmentsEntity -> userEnrolmentsEntity.getUserId().equals(userId))) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("User not enrolled in course");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        // validate role teacher can create Assignmet

        for (UserEnrolmentsEntity userEnrolments : userEnrolmentsEntityList) {
            if (userEnrolments.getUserId().equals(userId)) {
                if(userEnrolments.getEnrol().getCourseRole() == CourseRole.STUDENT){
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("User dont have permission to create Course Section");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }


        courseSectionsRepository.deleteById(id);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Success");
        return ResponseEntity.ok(resultDTO);
    }


}
