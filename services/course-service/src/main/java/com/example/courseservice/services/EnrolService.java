package com.example.courseservice.services;

import com.example.courseservice.dto.EnrolCreateDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.EnrolRepository;
import org.apache.catalina.valves.rewrite.InternalRewriteMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrolService {
    @Autowired
    EnrolRepository enrolRepository;
    @Autowired
    CourseRepository courseRepository;

    public ResponseEntity<ResultDTO> createSelfEnrol(EnrolCreateDTO enrolCreateDTO){
        ResultDTO resultDTO = new ResultDTO();
        EnrolEntity enrolEntity = new EnrolEntity();

        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(enrolCreateDTO.getCourse());
        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("course does not exist");
            return ResponseEntity.ok(resultDTO);
        }

        enrolEntity.setCourse(courseEntityOptional.get());
        enrolEntity.setStatus((short) 1);
        enrolEntity.setEnrolType(EnrolType.SELF);
        enrolEntity.setName(enrolCreateDTO.getName());
        enrolEntity.setPassword(enrolCreateDTO.getPassword());
        enrolEntity.setEnrolStartDate(enrolCreateDTO.getEnrolStartDate());
        enrolEntity.setEnrolEndDate(enrolCreateDTO.getEnrolEndDate());
        enrolEntity.setCourseRole(CourseRole.valueOf(enrolCreateDTO.getCourseRole()));

        EnrolEntity enrolResult = enrolRepository.save(enrolEntity);
        resultDTO.setStatus(1);
        resultDTO.setData(enrolResult);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getEnrolByCourseAndEnrolTypeManual(Long courseId, String enrolType){
        ResultDTO resultDTO = new ResultDTO();
        List<EnrolEntity> enrolEntities = enrolRepository.getEnrolEntitiesByCourseIdAndEnrolType(courseId, EnrolType.valueOf(enrolType));
        resultDTO.setStatus(1);
        resultDTO.setData(enrolEntities);
        return ResponseEntity.ok(resultDTO);
    }
}
