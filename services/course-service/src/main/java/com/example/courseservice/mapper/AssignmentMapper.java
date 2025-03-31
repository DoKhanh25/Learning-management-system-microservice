package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AssignmentDTO;
import com.example.courseservice.dto.AssignmentSubmissionsDTO;
import com.example.courseservice.entity.AssignmentEntity;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.AssignmentSubmissionsEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CourseMapper.class, AssignmentSubmissionsMapper.class})
public interface AssignmentMapper {

    @Named("toDto")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "assignmentSubmissions", source = "assignmentSubmissions", qualifiedByName = "mapSubmissions")
    AssignmentDTO toDto(AssignmentEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "assignmentSubmissions", ignore = true)
    AssignmentDTO toSimpleDto(AssignmentEntity entity, @Context CycleAvoidingMappingContext context);

    default Long map(AssignmentEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default AssignmentDTO toDtoWithoutContext(AssignmentEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "assignmentSubmissions", ignore = true)
    default AssignmentEntity toEntity(AssignmentDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        AssignmentEntity entity = new AssignmentEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setAssignmentType(dto.getAssignmentType());
        entity.setResubmit(dto.getResubmit());
        entity.setPreventLate(dto.getPreventLate());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        // Handle course reference by ID
        if (dto.getCourseId() != null) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setId(dto.getCourseId());
            entity.setCourse(courseEntity);
        }

        return entity;
    }

    @Named("mapSubmissions")
    default List<AssignmentSubmissionsDTO> mapSubmissions(List<AssignmentSubmissionsEntity> submissions, @Context CycleAvoidingMappingContext context) {
        if (submissions == null) return null;

        if (context.getMappedInstance(submissions, List.class) != null) {
            return (List<AssignmentSubmissionsDTO>) context.getMappedInstance(submissions, List.class);
        }

        List<AssignmentSubmissionsDTO> result = submissions.stream()
                .map(submission -> mapSubmission(submission, context))
                .toList();
        context.storeMappedInstance(submissions, result);
        return result;
    }

    @Mapping(target = "assignment", ignore = true) // Avoid circular reference
    AssignmentSubmissionsDTO mapSubmission(AssignmentSubmissionsEntity submission, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignmentSubmissions", ignore = true)
    default void updateAssignmentFromDto(AssignmentDTO dto, @MappingTarget AssignmentEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getAssignmentType() != null) {
            entity.setAssignmentType(dto.getAssignmentType());
        }
        if (dto.getResubmit() != null) {
            entity.setResubmit(dto.getResubmit());
        }
        if (dto.getPreventLate() != null) {
            entity.setPreventLate(dto.getPreventLate());
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }

        // Update course reference if courseId is provided
        if (dto.getCourseId() != null && (entity.getCourse() == null || !dto.getCourseId().equals(entity.getCourse().getId()))) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setId(dto.getCourseId());
            entity.setCourse(courseEntity);
        }
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<AssignmentDTO> toDtoList(List<AssignmentEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<AssignmentDTO> toDtoListWithoutContext(List<AssignmentEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }
}