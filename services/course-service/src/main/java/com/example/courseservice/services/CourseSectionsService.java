package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.mapper.CourseSectionsMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.CourseSectionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    CourseSectionsMapper courseSectionsMapper;

    public ResponseEntity<ResultDTO> getAllCourseSectionsByCourseId(Long courseId){
        ResultDTO resultDTO = new ResultDTO();
        List<CourseSectionsEntity> courseSectionsEntityList = courseSectionsRepository.getAllCourseSectionsByCourseId(courseId);
        resultDTO.setData(courseSectionsEntityList);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> addCourseSection(CourseSectionsDTO courseSectionsDTO){
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

    public ResponseEntity<ResultDTO> deleteCourseSectionById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseSectionsEntity> courseSectionsEntityOptional = courseSectionsRepository.findById(id);
        if(courseSectionsEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
        }
        courseSectionsRepository.deleteById(id);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Success");
        return ResponseEntity.ok(resultDTO);
    }


}
