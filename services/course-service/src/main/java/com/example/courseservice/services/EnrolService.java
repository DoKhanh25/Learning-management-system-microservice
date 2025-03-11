package com.example.courseservice.services;

import com.example.commondto.dto.CohortMemberDTO;
import com.example.courseservice.dto.EnrolDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.enums.EnrolType;
import com.example.courseservice.feign.UserServiceClient;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.EnrolRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EnrolService {
    @Autowired
    EnrolRepository enrolRepository;
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<ResultDTO> createSelfEnrol(EnrolDTO enrolDTO){
        ResultDTO resultDTO = new ResultDTO();
        EnrolEntity enrolEntity = new EnrolEntity();

        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(enrolDTO.getCourse());
        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("course does not exist");
            return ResponseEntity.ok(resultDTO);
        }

        enrolEntity.setCourse(courseEntityOptional.get());
        enrolEntity.setStatus((short) 1);
        enrolEntity.setEnrolType(EnrolType.SELF);
        enrolEntity.setName(enrolDTO.getName());
        enrolEntity.setPassword(enrolDTO.getPassword());
        enrolEntity.setEnrolStartDate(enrolDTO.getEnrolStartDate());
        enrolEntity.setEnrolEndDate(enrolDTO.getEnrolEndDate());
        enrolEntity.setCourseRole(enrolDTO.getCourseRole());

        EnrolEntity enrolResult = enrolRepository.save(enrolEntity);
        resultDTO.setStatus(1);
        resultDTO.setData(enrolResult);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getEnrolByCourseAndEnrolType(Long courseId, String enrolType){
        ResultDTO resultDTO = new ResultDTO();
        List<EnrolEntity> enrolEntities = enrolRepository.getEnrolEntitiesByCourseIdAndEnrolType(courseId, EnrolType.valueOf(enrolType));
        resultDTO.setStatus(1);
        resultDTO.setData(enrolEntities);
        return ResponseEntity.ok(resultDTO);
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "fallbackCohort")
    public ResponseEntity<ResultDTO> addEnrolmentsByCohort(Long cohortId, Long courseId, String courseRole){
        ResultDTO resultDTO = new ResultDTO();
        ResponseEntity<ResultDTO> response = userServiceClient.getCohortMemberEntitiesByCohortId(cohortId);

        if (response.getStatusCode().is2xxSuccessful()) {
            ResultDTO resultDTOResponse =  response.getBody();
            EnrolEntity enrolEntity = new EnrolEntity();

            if(resultDTOResponse == null || resultDTOResponse.getData() == null){
                resultDTO.setStatus(2);
                resultDTO.setMessage("No Cohort Found");
                return ResponseEntity.ok(resultDTO);
            }
            List<?> data = (List<?>) resultDTOResponse.getData();
            List<String> keycloakIds = new ArrayList<>();
            List<CohortMemberDTO> cohortMemberDTOList = objectMapper.convertValue(data, new TypeReference<List<CohortMemberDTO>>() {});

            if (cohortMemberDTOList.isEmpty()){
                resultDTO.setStatus(2);
                resultDTO.setMessage("No cohort Member");
                return ResponseEntity.ok(resultDTO);
            }

            for (CohortMemberDTO cohortMemberDTO: cohortMemberDTOList){
                keycloakIds.add(cohortMemberDTO.getKeycloakId());
            }

            Optional<CourseEntity> courseEntityOptional = courseRepository.findById(courseId);
            if(courseEntityOptional.isEmpty()){
                resultDTO.setStatus(2);
                resultDTO.setMessage("No Course Found");
                return ResponseEntity.ok(resultDTO);
            }

            // check User still active
            List<UserEnrolmentsEntity> userEnrolmentsEntityList = new ArrayList<>();
            for (CohortMemberDTO cohortMemberDTO: cohortMemberDTOList){
                if(cohortMemberDTO.getAvailable() == (short) 0){
                    resultDTO.setStatus(2);
                    resultDTO.setMessage("Tồn tại người dùng dừng hoạt động");
                    resultDTO.setData(cohortMemberDTO.getKeycloakId());
                    return ResponseEntity.ok(resultDTO);
                }

                UserEnrolmentsEntity userEnrolments = new UserEnrolmentsEntity();
                userEnrolments.setUserId(cohortMemberDTO.getKeycloakId());
                userEnrolments.setStatus(1);
                userEnrolments.setCreatedTime(new Date());
                userEnrolments.setTimeStart(new Date());
                userEnrolments.setEnrol(enrolEntity);
                userEnrolments.setTimeEnd(courseEntityOptional.get().getEndDate());
                userEnrolmentsEntityList.add(userEnrolments);
            }
            // check User is already in Enrolment
            List<UserEnrolmentsEntity> userEnrolmentsEntities = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(courseId);
            log.info(String.valueOf(userEnrolmentsEntities.size()));
            for (UserEnrolmentsEntity u: userEnrolmentsEntities){
                log.info(u.getUserId());
                if (keycloakIds.contains(u.getUserId())){
                    resultDTO.setStatus(2);
                    resultDTO.setMessage("Đã tồn tại người dùng trong khóa học");
                    resultDTO.setData(u.getUserId());
                    return ResponseEntity.ok(resultDTO);
                }
            }

            enrolEntity.setCourseRole(CourseRole.valueOf(courseRole));
            enrolEntity.setStatus((short) 1);
            enrolEntity.setCourse(courseEntityOptional.get());
            enrolEntity.setUserEnrolments(userEnrolmentsEntityList);

            EnrolEntity enrol = enrolRepository.save(enrolEntity);
            resultDTO.setStatus(1);
            resultDTO.setMessage("Success");
            resultDTO.setData(enrol);
            return ResponseEntity.ok(resultDTO);

        } else {
            throw new RuntimeException("Không thể lấy dữ liệu cohort từ User-service");
        }
    }



    public ResponseEntity<ResultDTO> fallbackCohort(Long cohortId, Long courseId, String courseRole, Throwable t) {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(0);
        resultDTO.setMessage(t.getMessage());
        resultDTO.setData("Không thể kết nối tới User-service");
        return ResponseEntity.ok(resultDTO);
    }

}
