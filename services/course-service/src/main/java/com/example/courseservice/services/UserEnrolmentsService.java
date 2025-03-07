package com.example.courseservice.services;

import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public ResponseEntity<ResultDTO> getAllUserEnrolmentsByCourseId(Long id){
        ResultDTO resultDTO = new ResultDTO();
        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(id);
        resultDTO.setStatus(1);
        resultDTO.setData(userEnrolmentsEntityList);
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
}
