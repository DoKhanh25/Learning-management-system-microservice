package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.*;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.CourseSectionsRepository;
import com.example.courseservice.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    CourseSectionsRepository courseSectionsRepository;

    @Autowired
    CourseMapper courseMapper;
    @Autowired
    private LessonMapper lessonMapper;

    public ResponseEntity<ResultDTO> getAllCourses(){
        ResultDTO resultDTO = new ResultDTO();
        List<CourseEntity> courseEntities = courseRepository.findAll();
        resultDTO.setData(courseEntities);
        resultDTO.setStatus(1);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> addCourse(CourseCreateDTO courseCreateDTO){
        ResultDTO resultDTO = new ResultDTO();
        CourseEntity courseEntity = new CourseEntity();
        if(courseCreateDTO.getName() == null ||
                courseCreateDTO.getName().isEmpty() ||
                courseCreateDTO.getStartDate() == null ||
                courseCreateDTO.getEndDate() == null)
        {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Empty Date");
            return ResponseEntity.ok(resultDTO);
        }

        courseEntity.setName(courseCreateDTO.getName());
        courseEntity.setSummary(courseCreateDTO.getSummary());
        courseEntity.setStartDate(courseCreateDTO.getStartDate());
        courseEntity.setEndDate(courseCreateDTO.getEndDate());
        courseEntity.setShowGrades(courseCreateDTO.getShowGrades());

        CourseEntity courseEntityResult = courseRepository.save(courseEntity);
        resultDTO.setStatus(1);
        resultDTO.setData(courseEntityResult);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getCourseById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(id);

        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(courseMapper.toDto(courseEntityOptional.get(), new CycleAvoidingMappingContext()));
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteCourseById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(id);
        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }
        courseRepository.deleteById(id);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllTeacherCoursesByUserId(String userId){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        List<CourseEntity> courseEntities = courseRepository.getAllTeacherCoursesByUserId(userId);
        List<CourseDTO> courseDTOList = new ArrayList<>();
        for (CourseEntity c: courseEntities){
            c.setShowGrades((short) courseRepository.countAllByCourseIdAndCourseRoleStudent(c.getId()));
            courseDTOList.add(courseMapper.toSimpleDto(c, new CycleAvoidingMappingContext()));
        }

        resultDTO.setData(courseDTOList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllStudentCoursesByUserId(String userId){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        List<CourseEntity> courseEntities = courseRepository.getAllStudentCoursesByUserId(userId);
        List<CourseDTO> courseDTOList = new ArrayList<>();

        for (CourseEntity c: courseEntities){
            courseDTOList.add(courseMapper.toSimpleDto(c, new CycleAvoidingMappingContext()));
        }

        for (CourseDTO c: courseDTOList){
            c.setEnrols(null);
            c.setResources(null);
        }

        resultDTO.setData(courseDTOList);
        return ResponseEntity.ok(resultDTO);

    }

    public ResponseEntity<ResultDTO> getTeacherCourseById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(id);

        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }

        CourseEntity courseEntity = courseEntityOptional.get();

        CourseDTO courseDTO = courseMapper.toDto(courseEntity, new CycleAvoidingMappingContext());
        List<CourseSectionsDTO> courseSectionsDTOList = courseDTO.getCourseSections();

        for (CourseSectionsDTO courseSectionsDTO: courseSectionsDTOList){
            List<LessonEntity> lessonEntities = lessonRepository.findLessonEntitiesBySectionId(courseSectionsDTO.getId());
            List<LessonDTO> lessonDTOS = new ArrayList<>();
            for (LessonEntity lessonEntity: lessonEntities){
                lessonDTOS.add(lessonMapper.toDto(lessonEntity, new CycleAvoidingMappingContext()));
            }
            courseSectionsDTO.setLessons(lessonDTOS);
        }
        courseDTO.setCourseSections(courseSectionsDTOList);
        resultDTO.setData(courseDTO);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }


}
