package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonNoteDTO;
import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonNoteEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.LessonNoteMapper;
import com.example.courseservice.repository.LessonNoteRepository;
import com.example.courseservice.repository.LessonPagesRepository;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class LessonNoteService {

    @Autowired
    LessonNoteRepository lessonNoteRepository;

    @Autowired
    LessonNoteMapper lessonNoteMapper;

    @Autowired
    LessonPagesRepository lessonPagesRepository;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    public ResponseEntity<ResultDTO> getAllLessonNotesByLessonPageId(Long lessonPagesId, String userId, String roles){
        List<String> rolesList = Arrays.asList(roles.split(","));
        ResultDTO resultDTO = new ResultDTO();

        CourseRole courseRole = lessonNoteRepository.findCourseRoleByUserIdAndLessonPagesId(userId, lessonPagesId);
        log.info("courseRole: " + courseRole.toString());
        if(rolesList.contains("ROLE_ADMIN") || courseRole.equals(CourseRole.TEACHER)){
            List<LessonNoteEntity> lessonNoteEntityList = lessonNoteRepository.getAllLessonNotesByLessonPageId(lessonPagesId);
            resultDTO.setStatus(1);
            resultDTO.setMessage("Success");
            resultDTO.setData(lessonNoteMapper.toDtoList(lessonNoteEntityList, new CycleAvoidingMappingContext()));
            return ResponseEntity.ok(resultDTO);
        }
        resultDTO.setStatus(0);
        resultDTO.setMessage("You dont have permission to access this page");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getLessonNoteByLessonPageIdAndUserId(Long lessonPagesId, String userId){
        ResultDTO resultDTO = new ResultDTO();
        LessonNoteEntity lessonNoteEntity = lessonNoteRepository.getLessonNoteByLessonPageIdAndUserId(userId, lessonPagesId);
        if(lessonNoteEntity != null){
            resultDTO.setStatus(1);
            resultDTO.setMessage("Success");
            resultDTO.setData(lessonNoteMapper.toDto(lessonNoteEntity, new CycleAvoidingMappingContext()));
            return ResponseEntity.ok(resultDTO);
        }
        resultDTO.setStatus(0);
        resultDTO.setMessage("Not found");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> saveLessonNote(LessonNoteDTO lessonNoteDTO, String userId) {
        ResultDTO resultDTO = new ResultDTO();
        LessonNoteEntity lessonNoteEntity = lessonNoteMapper.toEntity(lessonNoteDTO, new CycleAvoidingMappingContext());

        LessonNoteEntity lessonNoteEntityExist = lessonNoteRepository.getLessonNoteByLessonPageIdAndUserId(userId, lessonNoteDTO.getLessonPagesId());
        if(lessonNoteEntityExist != null){

            lessonNoteEntityExist.setNote(lessonNoteDTO.getNote());
            lessonNoteEntityExist.setUpdatedTime(new Date());
            lessonNoteEntity = lessonNoteRepository.save(lessonNoteEntityExist);

            resultDTO.setStatus(1);
            resultDTO.setMessage("update success");
            resultDTO.setData(lessonNoteMapper.toDto(lessonNoteEntity, new CycleAvoidingMappingContext()));

            return ResponseEntity.ok(resultDTO);
        }

        LessonPagesEntity lessonPagesEntity = lessonPagesRepository.findById(lessonNoteDTO.getLessonPagesId()).orElse(null);
        if(lessonPagesEntity == null){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Not found");
            return ResponseEntity.ok(resultDTO);
        }


        lessonNoteEntity.setUserId(userId);
        lessonNoteEntity.setLessonPages(lessonPagesEntity);
        lessonNoteEntity.setCreatedTime(new Date());
        LessonNoteEntity lessonNote = lessonNoteRepository.save(lessonNoteEntity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Success");
        resultDTO.setData(lessonNoteMapper.toDto(lessonNote, new CycleAvoidingMappingContext()));

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteLessonNote(Long lessonNoteId, String userId) {
        LessonNoteEntity lessonNoteEntity = lessonNoteRepository.getLessonNoteByUserId(userId, lessonNoteId);
        ResultDTO resultDTO = new ResultDTO();

        if (lessonNoteEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Not found");
            return ResponseEntity.ok(resultDTO);
        }
        if (lessonNoteEntity.getId().equals(lessonNoteId)) {
            lessonNoteRepository.deleteById(lessonNoteId);
            resultDTO.setStatus(1);
            resultDTO.setMessage("Success");
            return ResponseEntity.ok(resultDTO);
        }
        resultDTO.setStatus(0);
        resultDTO.setMessage("Not found");
        return ResponseEntity.ok(resultDTO);
    }

}
