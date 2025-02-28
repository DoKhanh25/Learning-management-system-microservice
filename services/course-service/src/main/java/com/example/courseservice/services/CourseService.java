package com.example.courseservice.services;

import com.example.courseservice.dto.CourseCreateDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    CourseRepository courseRepository;

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
        resultDTO.setData(courseEntityOptional.get());
        return ResponseEntity.ok(resultDTO);
    }
}
