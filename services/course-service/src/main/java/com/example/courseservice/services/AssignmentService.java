package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AssignmentDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.AssignmentEntity;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.AssignmentMapper;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.repository.AssignmentRepository;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.EnrolRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrolRepository enrolRepository;

    @Autowired
    private UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    AssignmentMapper assignmentMapper;


    public ResponseEntity<ResultDTO> getAllAssignmentsByCourseId(Long courseId, String userId){
        ResultDTO resultDTO = new ResultDTO();
        if(courseId == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course ID is required");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);

        if(courseEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
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

        List<AssignmentEntity> assignmentEntityList = assignmentRepository.findAssignmentEntitiesByCourseId(courseId);
        List<AssignmentDTO> assignmentDTOList = assignmentMapper.toDtoList(assignmentEntityList, new CycleAvoidingMappingContext());

        resultDTO.setStatus(1);
        resultDTO.setMessage("success");
        resultDTO.setData(assignmentDTOList);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }


    public ResponseEntity<ResultDTO> getAssignmentById(Long assignmentId, String userId){
        ResultDTO resultDTO = new ResultDTO();

        if(assignmentId == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course ID is required");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        Optional<AssignmentEntity> assignmentEntityOptional = assignmentRepository.findById(assignmentId);
        if(assignmentEntityOptional.isEmpty()){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AssignmentEntity assignmentEntity = assignmentEntityOptional.get();

        CourseEntity courseEntity = assignmentEntity.getCourse();

        if(courseEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
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

        AssignmentDTO assignmentDTO = assignmentMapper.toDto(assignmentEntity, new CycleAvoidingMappingContext());

        resultDTO.setStatus(1);
        resultDTO.setMessage("success");
        resultDTO.setData(assignmentDTO);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }



    public ResponseEntity<ResultDTO> addAssignment(AssignmentDTO assignmentDTO, String userId) {
        ResultDTO resultDTO = new ResultDTO();

        if(assignmentDTO.getCourseId() == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course ID is required");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        CourseEntity courseEntity = courseRepository.findById(assignmentDTO.getCourseId()).orElse(null);

        if(courseEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
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
                    resultDTO.setMessage("User dont have permission to create Assignment");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }

        AssignmentEntity assignmentEntity = assignmentMapper.toEntity(assignmentDTO, new CycleAvoidingMappingContext());
        assignmentEntity.setCourse(courseEntity);
        assignmentEntity = assignmentRepository.save(assignmentEntity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Assignment created");
        resultDTO.setData(assignmentEntity);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateAssignment(AssignmentDTO assignmentDTO, String userId) {
        ResultDTO resultDTO = new ResultDTO();

        Optional<AssignmentEntity> assignmentEntityOptional = assignmentRepository.findById(assignmentDTO.getId());
        if(assignmentEntityOptional.isEmpty()){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AssignmentEntity assignmentEntity = assignmentEntityOptional.get();

        CourseEntity courseEntity = assignmentEntity.getCourse();

        if(courseEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
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

        for (UserEnrolmentsEntity userEnrolments : userEnrolmentsEntityList) {
            if (userEnrolments.getUserId().equals(userId)) {
                if(userEnrolments.getEnrol().getCourseRole() == CourseRole.STUDENT){
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("User dont have permission to delete Assignment");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }

        assignmentEntity.setDescription(assignmentDTO.getDescription());
        assignmentEntity.setPreventLate(assignmentDTO.getPreventLate());
        assignmentEntity.setResubmit(assignmentDTO.getResubmit());
        assignmentEntity.setStartDate(assignmentDTO.getStartDate());
        assignmentEntity.setEndDate(assignmentDTO.getEndDate());
        assignmentEntity = assignmentRepository.save(assignmentEntity);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Assignment updated");
        resultDTO.setData(assignmentEntity);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteAssignmentById(Long assignmentId, String userId){
        ResultDTO resultDTO = new ResultDTO();

        if(assignmentId == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course ID is required");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        Optional<AssignmentEntity> assignmentEntityOptional = assignmentRepository.findById(assignmentId);
        if(assignmentEntityOptional.isEmpty()){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AssignmentEntity assignmentEntity = assignmentEntityOptional.get();

        CourseEntity courseEntity = assignmentEntity.getCourse();

        if(courseEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
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

        for (UserEnrolmentsEntity userEnrolments : userEnrolmentsEntityList) {
            if (userEnrolments.getUserId().equals(userId)) {
                if(userEnrolments.getEnrol().getCourseRole() == CourseRole.STUDENT){
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("User dont have permission to delete Assignment");
                    return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
                }
            }
        }

        assignmentRepository.deleteById(assignmentId);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Assignment deleted");
        return ResponseEntity.ok(resultDTO);
    }


}
