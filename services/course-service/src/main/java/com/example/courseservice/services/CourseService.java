package com.example.courseservice.services;

import com.example.commondto.dto.TreeGridNodeDTO;
import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.*;
import com.example.courseservice.entity.*;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonPagesRepository lessonPagesRepository;

    @Autowired
    CourseSectionsRepository courseSectionsRepository;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    private LessonMapper lessonMapper;

    public ResponseEntity<ResultDTO> getAllCourses(){
        ResultDTO resultDTO = new ResultDTO();
        List<CourseEntity> courseEntities = courseRepository.findAll();
        List<CourseDTO> courseDTOs = new ArrayList<>();
        for (CourseEntity courseEntity : courseEntities) {
            courseDTOs.add(courseMapper.toSimpleDto(courseEntity, new CycleAvoidingMappingContext()));
        }

        resultDTO.setData(courseDTOs);
        resultDTO.setStatus(1);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllCourseAvailable(String userId){
        ResultDTO resultDTO = new ResultDTO();

        List<CourseEntity> attendedCourseEntities = userEnrolmentsRepository.getCoursesByUserId(userId);
        List<CourseEntity> courseEntities = courseRepository.findAll();
        List<CourseAvailableDTO> courseAvailableDTOs = new ArrayList<>();

        for (CourseEntity courseEntity : attendedCourseEntities) {
            CourseAvailableDTO courseAvailableDTO = new CourseAvailableDTO();
            courseAvailableDTO.setId(courseEntity.getId());
            courseAvailableDTO.setName(courseEntity.getName());
            courseAvailableDTO.setAttended(true);
            courseAvailableDTO.setStartDate(courseEntity.getStartDate());
            courseAvailableDTO.setEndDate(courseEntity.getEndDate());
            courseAvailableDTOs.add(courseAvailableDTO);
        }

        for (CourseEntity courseEntity : courseEntities) {
            if(!attendedCourseEntities.contains(courseEntity)) {
                CourseAvailableDTO courseAvailableDTO = new CourseAvailableDTO();
                courseAvailableDTO.setId(courseEntity.getId());
                courseAvailableDTO.setName(courseEntity.getName());
                courseAvailableDTO.setAttended(false);
                courseAvailableDTO.setStartDate(courseEntity.getStartDate());
                courseAvailableDTO.setEndDate(courseEntity.getEndDate());
                courseAvailableDTOs.add(courseAvailableDTO);
            }
        }

        resultDTO.setData(courseAvailableDTOs);
        resultDTO.setStatus(1);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAttendedCoursesByUserId(String userId){
        ResultDTO resultDTO = new ResultDTO();
        List<CourseEntity> attendedCourseEntities = userEnrolmentsRepository.getCoursesByUserId(userId);
        resultDTO.setData(attendedCourseEntities);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateCourse(CourseCreateDTO courseDTO, Long courseId){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(courseId);

        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Course not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        CourseEntity courseEntity = courseEntityOptional.get();
        courseEntity.setName(courseDTO.getName());
        courseEntity.setSummary(courseDTO.getSummary());
        courseEntity.setStartDate(courseDTO.getStartDate());
        courseEntity.setEndDate(courseDTO.getEndDate());

        courseEntity = courseRepository.save(courseEntity);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Course updated");
        resultDTO.setData(courseEntity);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
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
        resultDTO.setData(courseMapper.toDto(courseEntityOptional.get(), new CycleAvoidingMappingContext()));
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteCourseById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(id);
        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }
        courseRepository.deleteById(id);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllTeacherCoursesByUserId(String userId){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        List<CourseEntity> courseEntities = courseRepository.getAllTeacherCoursesByUserId(userId);
        List<CourseDTO> courseDTOList = new ArrayList<>();
        for (CourseEntity c: courseEntities){
            c.setShowGrades((short) courseRepository.countAllByCourseIdAndCourseRoleStudent(c.getId()));
            courseDTOList.add(courseMapper.toSimpleDto(c, new CycleAvoidingMappingContext()));
        }

        resultDTO.setData(courseDTOList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAllStudentCoursesByUserId(String userId){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        List<CourseEntity> courseEntities = courseRepository.getAllStudentCoursesByUserId(userId);
        List<CourseDTO> courseDTOList = new ArrayList<>();

        for (CourseEntity c: courseEntities){
            courseDTOList.add(courseMapper.toSimpleDto(c, new CycleAvoidingMappingContext()));
        }

        for (CourseDTO c: courseDTOList){
            c.setEnrols(null);
            c.setResources(null);
        }
        resultDTO.setData(courseDTOList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getTeacherCourseById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(id);

        if(courseEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }

        CourseEntity courseEntity = courseEntityOptional.get();

        CourseDTO courseDTO = courseMapper.toDto(courseEntity, new CycleAvoidingMappingContext());
        List<CourseSectionsDTO> courseSectionsDTOList = courseDTO.getCourseSections();

        for (CourseSectionsDTO courseSectionsDTO: courseSectionsDTOList){
            List<LessonEntity> lessonEntities = lessonRepository.findLessonEntitiesBySectionId(courseSectionsDTO.getId());
            List<LessonDTO> lessonDTOS = new ArrayList<>();
            for (LessonEntity lessonEntity: lessonEntities){
                lessonDTOS.add(lessonMapper.toDto(lessonEntity, new CycleAvoidingMappingContext()));
            }
            courseSectionsDTO.setLessons(lessonDTOS);
        }
        courseDTO.setCourseSections(courseSectionsDTOList);
        resultDTO.setData(courseDTO);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getAssignmentsTree(Long courseId){
        ResultDTO resultDTO = new ResultDTO();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
        List<TreeGridNodeDTO> treeGridNodeDTOList = new ArrayList<>();

        if(courseEntity == null){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.notFound().build();
        }

        List<AssignmentEntity> assignmentEntityList = assignmentRepository.findAssignmentEntitiesByCourseId(courseId);
        for (AssignmentEntity assignmentEntity: assignmentEntityList){
            TreeGridNodeDTO treeGridNodeDTO = new TreeGridNodeDTO();
            treeGridNodeDTO.setId(assignmentEntity.getId());
            treeGridNodeDTO.setParentId(null);
            treeGridNodeDTO.setType("assignment");
            treeGridNodeDTO.setTitle(assignmentEntity.getName());
            treeGridNodeDTOList.add(treeGridNodeDTO);
        }
        resultDTO.setData(treeGridNodeDTOList);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);

    }

    public ResponseEntity<ResultDTO> getLessonPagesTree(Long courseId){
        ResultDTO resultDTO = new ResultDTO();
        List<TreeGridNodeDTO> treeGridNodeDTOList = new ArrayList<>();

        CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);

        if(courseEntity == null){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.notFound().build();
        }

        List<CourseSectionsEntity> courseSectionsEntities = courseEntity.getCourseSections();
        for (CourseSectionsEntity cs: courseSectionsEntities){
            // Add course section node
            TreeGridNodeDTO sectionNode = new TreeGridNodeDTO();
            sectionNode.setId(cs.getId());
            sectionNode.setTitle(cs.getName());
            sectionNode.setType("courseSection");
            sectionNode.setParentId(null); // Course sections are top level
            treeGridNodeDTOList.add(sectionNode);
            
            // Add lesson nodes with section as parent
            List<LessonEntity> lessonEntities = lessonRepository.findLessonEntitiesBySectionId(cs.getId());
            for (LessonEntity lesson: lessonEntities){
                TreeGridNodeDTO lessonNode = new TreeGridNodeDTO();
                lessonNode.setId(lesson.getId());
                lessonNode.setTitle(lesson.getName());
                lessonNode.setParentId(cs.getId());
                lessonNode.setType("lesson");
                treeGridNodeDTOList.add(lessonNode);

//                for (LessonPagesEntity page : lesson.getLessonPages()) {
//                    TreeGridNodeDTO pageNode = new TreeGridNodeDTO();
//                    pageNode.setId(page.getId());
//                    pageNode.setTitle(page.getTitle());
//                    pageNode.setParentId(lesson.getId());
//                    pageNode.setType("lessonPage");
//                    treeGridNodeDTOList.add(pageNode);
//                }
           }
        }
        resultDTO.setData(treeGridNodeDTOList);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

}
