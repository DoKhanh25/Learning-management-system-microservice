package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.EnrolDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.dto.UserEnrolmentsDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.EnrolMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.EnrolRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserEnrolmentsService {
    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrolMapper enrolMapper;
    @Autowired
    private EnrolRepository enrolRepository;

    public ResponseEntity<ResultDTO> getAllUserEnrolmentsByCourseId(Long id){
        ResultDTO resultDTO = new ResultDTO();
        List<UserEnrolmentsDTO> userEnrolmentsDTOList = new ArrayList<>();
        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(id);
        for (UserEnrolmentsEntity u: userEnrolmentsEntityList){
            userEnrolmentsDTOList.add(enrolMapper.toDto(u, new CycleAvoidingMappingContext()));
        }

        resultDTO.setStatus(1);
        resultDTO.setData(userEnrolmentsDTOList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllStudentEnrolmentsByCourseId(Long id){
        ResultDTO resultDTO = new ResultDTO();
        List<UserEnrolmentsDTO> userEnrolmentsDTOList = new ArrayList<>();
        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllStudentEnrolmentsByCourseId(id);
        for (UserEnrolmentsEntity u: userEnrolmentsEntityList){
            userEnrolmentsDTOList.add(enrolMapper.toDto(u, new CycleAvoidingMappingContext()));
        }

        resultDTO.setStatus(1);
        resultDTO.setData(userEnrolmentsDTOList);
        return ResponseEntity.ok(resultDTO);
    }



    public ResponseEntity<ResultDTO> updateUserEnrolmentsManual(Long courseId, List<String> userIds){
        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(courseId);
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(courseId);
        ResultDTO resultDTO = new ResultDTO();

        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data Course");
            return ResponseEntity.ok(resultDTO);
        }

        CourseEntity courseEntity = courseEntityOptional.get();

        if (userEnrolmentsEntityList.isEmpty()){
            if(userIds.isEmpty()){
                resultDTO.setStatus(2);
                resultDTO.setMessage("No data user");
                return ResponseEntity.ok(resultDTO);
            }

            List<UserEnrolmentsEntity> userEnrolmentsEntities = new ArrayList<>();
            EnrolEntity enrol = new EnrolEntity();
            enrol.setEnrolStartDate(courseEntity.getStartDate());
            enrol.setEnrolEndDate(courseEntity.getEndDate());
            enrol.setCourse(courseEntity);
            enrol.setPassword(null);
            enrol.setStatus((short) 1);
            enrol.setCourseRole(CourseRole.STUDENT);


            for (String userId: userIds){
                UserEnrolmentsEntity userEnrolments = new UserEnrolmentsEntity();
                userEnrolments.setUserId(userId);
                userEnrolments.setStatus(1);
                userEnrolments.setCreatedTime(new Date());
                userEnrolmentsEntities.add(userEnrolments);
            }


        } else {

        }
        return null;
    }

    public ResponseEntity<ResultDTO> addSelfUserEnrolment(EnrolDTO enrolDTO, String userId){
        ResultDTO resultDTO = new ResultDTO();
        if(enrolDTO.getCourse() == null){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data Course");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        EnrolEntity enrol = enrolRepository.getEnrolEntitiesByCourseIdAndPasswordAndEnrolType(enrolDTO.getCourse(), enrolDTO.getPassword(), enrolDTO.getCourseRole());

        if(enrol == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("password is wrong");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(enrol.getEnrolStartDate().after(new Date())){
            resultDTO.setStatus(0);
            resultDTO.setMessage("enrol is not started");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(enrol.getEnrolEndDate().before(new Date())){
            resultDTO.setStatus(0);
            resultDTO.setMessage("enrol is not ended");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        List<UserEnrolmentsEntity> userEnrolmentsEntities = userEnrolmentsRepository.getUserEnrolmentsByCourseId(enrol.getCourse().getId());

        List<String> userIds = new ArrayList<>();

        for (UserEnrolmentsEntity userEnrolmentsEntity: userEnrolmentsEntities){
            userIds.add(userEnrolmentsEntity.getUserId());
        }

        if(userIds.contains(userId)){
            resultDTO.setStatus(0);
            resultDTO.setMessage("user already exists");
            return new ResponseEntity<>(resultDTO, HttpStatus.CONFLICT);
        }

        UserEnrolmentsEntity userEnrolmentsEntity = new UserEnrolmentsEntity();
        userEnrolmentsEntity.setUserId(userId);
        userEnrolmentsEntity.setStatus(1);
        userEnrolmentsEntity.setCreatedTime(new Date());
        userEnrolmentsEntity.setEnrol(enrol);

        userEnrolmentsEntity = userEnrolmentsRepository.save(userEnrolmentsEntity);

        resultDTO.setStatus(1);
        resultDTO.setData(userEnrolmentsEntity);
        return ResponseEntity.ok(resultDTO);
    }
}
