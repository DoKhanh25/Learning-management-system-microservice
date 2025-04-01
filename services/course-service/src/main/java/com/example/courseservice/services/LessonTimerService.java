package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonTimerDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.*;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.mapper.LessonTimerMapper;
import com.example.courseservice.repository.EnrolRepository;
import com.example.courseservice.repository.LessonRepository;
import com.example.courseservice.repository.LessonTimerRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LessonTimerService {
    @Autowired
    LessonTimerRepository lessonTimerRepository;

    @Autowired
    EnrolRepository enrolRepository;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonTimerMapper lessonTimerMapper;

    public ResponseEntity<ResultDTO> getAllLessonTimers(Long lessonId, String userId) {
        ResultDTO resultDTO = new ResultDTO();
        CourseRole courseRole = userEnrolmentsRepository.getCourseRoleByUserId(userId);

        if(courseRole == CourseRole.STUDENT){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        };

        List<LessonTimerEntity> lessonTimerEntities = lessonTimerRepository.findLessonTimerEntitiesByLessonId(lessonId);
        List<LessonTimerDTO> lessonTimerDTOList = lessonTimerMapper.toDtoList(lessonTimerEntities, new CycleAvoidingMappingContext());
        resultDTO.setStatus(1);
        resultDTO.setData(lessonTimerDTOList);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> saveLessonTimer(LessonTimerDTO lessonTimerDTO, String userId) {
        // validate user in course have lesson
        ResultDTO resultDTO = new ResultDTO();

        if(lessonTimerDTO.getLessonId() == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        CourseEntity courseEntity = lessonRepository.findCourseEntityByLessonId(lessonTimerDTO.getLessonId());

        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseEntity.getId());

        if(enrolEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        LessonEntity lessonEntity = lessonRepository.findById(lessonTimerDTO.getLessonId()).orElse(null);
        if(lessonEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("No such lesson found");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        LessonTimerEntity lessonTimerEntity = lessonTimerRepository.findLessonTimerEntityByLessonIdAndUserId(lessonTimerDTO.getLessonId(), userId);

        if(lessonTimerEntity == null){
            LessonTimerEntity newLessonTimerEntity = lessonTimerMapper.toEntity(lessonTimerDTO, new CycleAvoidingMappingContext());
            newLessonTimerEntity.setLesson(lessonEntity);
            newLessonTimerEntity.setUserId(userId);
            newLessonTimerEntity.setStartTime(new Date());
            newLessonTimerEntity = lessonTimerRepository.save(newLessonTimerEntity);
            resultDTO.setStatus(1);
            resultDTO.setData(lessonTimerMapper.toDto(newLessonTimerEntity, new CycleAvoidingMappingContext()));
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }

        if(lessonTimerEntity.getCompleted() != 1){
            lessonTimerEntity.setCompleted(lessonTimerDTO.getCompleted());
            lessonTimerEntity = lessonTimerRepository.save(lessonTimerEntity);
            resultDTO.setStatus(1);
            resultDTO.setData(lessonTimerMapper.toDto(lessonTimerEntity, new CycleAvoidingMappingContext()));
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }
        return null;
    }
}
