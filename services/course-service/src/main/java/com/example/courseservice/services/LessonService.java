package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.CourseSectionsRepository;
import com.example.courseservice.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class LessonService {
    @Autowired
    LessonRepository lessonRepository;
    
    @Autowired
    CourseSectionsRepository courseSectionsRepository;
    
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    LessonMapper lessonMapper;

    public ResponseEntity<ResultDTO> findLessonEntitiesBySectionId(Long sectionId){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        resultDTO.setData(lessonRepository.findLessonEntitiesBySectionId(sectionId));
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> findLessonEntitiesByCourseId(Long id){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        resultDTO.setData(lessonRepository.findLessonEntitiesByCourseId(id));
        return ResponseEntity.ok(resultDTO);
    }

    
    public ResponseEntity<ResultDTO> addLesson(LessonDTO lessonDTO) {
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseSectionsEntity> courseSectionsEntityOptional = courseSectionsRepository.findById(lessonDTO.getSectionId());

        if (lessonDTO.getName() == null || lessonDTO.getName().trim().isEmpty()) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Lesson name is required.");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        if (lessonDTO.getSectionId() == null || courseSectionsEntityOptional.isEmpty()) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid or missing section ID.");
            return ResponseEntity.badRequest().body(resultDTO);
        }


        LessonEntity lessonEntity = lessonMapper.toEntity(lessonDTO, new CycleAvoidingMappingContext());
        lessonEntity.setSection(courseSectionsEntityOptional.get());
        lessonEntity.setCreatedTime(new Date());

        lessonEntity = lessonRepository.save(lessonEntity);
        resultDTO.setData(lessonEntity);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);

    }
    

}
