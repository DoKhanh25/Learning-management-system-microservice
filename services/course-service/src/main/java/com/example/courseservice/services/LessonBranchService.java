package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonBranchDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.dto.StudentProgressDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.LessonBranchEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.LessonBranchMapper;
import com.example.courseservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonBranchService {
    @Autowired
    LessonBranchMapper lessonBranchMapper;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonPagesRepository lessonPagesRepository;

    @Autowired
    LessonBranchRepository lessonBranchRepository;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    public ResponseEntity<ResultDTO> getStudentProgressByLessonIdAndUserId(Long lessonId, String userId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        CourseRole courseRole = userEnrolmentsRepository.getCourseRoleByUserId(validateUserId);
        CourseEntity courseEntity = lessonRepository.findCourseEntityByLessonId(lessonId);
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(validateUserId, courseEntity.getId());


        if(enrolEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(courseRole == CourseRole.STUDENT) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        List<StudentProgressDTO> studentProgressDTOList = lessonBranchRepository.getStudentProgressByLessonPagesIdAndUserId(lessonId, userId);
        studentProgressDTOList.removeIf(studentProgressDTO -> studentProgressDTO.getStartTime() == null);

        resultDTO.setStatus(1);
        resultDTO.setData(studentProgressDTOList);

        return ResponseEntity.status(HttpStatus.OK).body(resultDTO);
    }

    public ResponseEntity<ResultDTO> getStudentProgressByLessonId(Long lessonId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        CourseRole courseRole = userEnrolmentsRepository.getCourseRoleByUserId(validateUserId);
        CourseEntity courseEntity = lessonRepository.findCourseEntityByLessonId(lessonId);
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(validateUserId, courseEntity.getId());


        if(enrolEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(courseRole == CourseRole.STUDENT) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        List<StudentProgressDTO> studentProgressDTOList = lessonBranchRepository.getStudentProgressByLessonId(lessonId);
        resultDTO.setStatus(1);
        resultDTO.setData(studentProgressDTOList);

        return ResponseEntity.status(HttpStatus.OK).body(resultDTO);
    }


    public ResponseEntity<ResultDTO> saveLessonBranch(LessonBranchDTO lessonBranchDTO,
                                                      String userId) {
        ResultDTO resultDTO = new ResultDTO();

        if(lessonBranchDTO.getLessonPagesId() == null || lessonBranchDTO.getLessonId() == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        CourseEntity courseEntity = lessonRepository.findCourseEntityByLessonId(lessonBranchDTO.getLessonId());

        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseEntity.getId());

        if(enrolEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to view this page");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        LessonBranchEntity lessonBranchEntity = lessonBranchRepository.findLessonBranchEntityByLessonPagesIdAndUserId(lessonBranchDTO.getLessonPagesId(), userId);
        if(lessonBranchEntity == null){
            LessonBranchEntity newLessonBranchEntity = lessonBranchMapper.toEntity(lessonBranchDTO, new CycleAvoidingMappingContext());

            LessonPagesEntity lessonPagesEntity = lessonPagesRepository.findById(lessonBranchDTO.getLessonPagesId()).orElse(null);

            if(lessonPagesEntity == null){
                resultDTO.setStatus(0);
                resultDTO.setMessage("You dont have permission to view this page");
                return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
            }

            newLessonBranchEntity.setUserId(userId);
            newLessonBranchEntity.setLessonPages(lessonPagesEntity);
            newLessonBranchEntity = lessonBranchRepository.save(newLessonBranchEntity);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Successfully added lesson branch");
            resultDTO.setData(lessonBranchMapper.toDto(newLessonBranchEntity, new CycleAvoidingMappingContext()));
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }

        lessonBranchEntity.setTimeSeen(lessonBranchEntity.getTimeSeen() + lessonBranchDTO.getTimeSeen());
        lessonBranchEntity = lessonBranchRepository.save(lessonBranchEntity);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Successfully added lesson branch");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
}
