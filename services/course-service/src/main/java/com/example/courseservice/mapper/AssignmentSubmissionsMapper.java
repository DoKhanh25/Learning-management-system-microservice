package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AssignmentSubmissionsDTO;
import com.example.courseservice.entity.AssignmentSubmissionsEntity;
import com.example.courseservice.entity.AssignmentEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AssignmentMapper.class})
public interface AssignmentSubmissionsMapper {

    @Named("toDto")
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "assignment", source = "assignment", qualifiedByName = "simple")
    AssignmentSubmissionsDTO toDto(AssignmentSubmissionsEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "assignment", ignore = true)
    AssignmentSubmissionsDTO toSimpleDto(AssignmentSubmissionsEntity entity, @Context CycleAvoidingMappingContext context);

    default Long map(AssignmentSubmissionsEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default AssignmentSubmissionsDTO toDtoWithoutContext(AssignmentSubmissionsEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    default AssignmentSubmissionsEntity toEntity(AssignmentSubmissionsDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        AssignmentSubmissionsEntity entity = new AssignmentSubmissionsEntity();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setNumfiles(dto.getNumfiles());
        entity.setData1(dto.getData1());
        entity.setData2(dto.getData2());
        entity.setGrade(dto.getGrade());
        entity.setSubmissionComment(dto.getSubmissionComment());
        entity.setCreatedTime(dto.getCreatedTime());
        entity.setUpdatedTime(dto.getUpdatedTime());

        // Handle assignment reference by ID
        if (dto.getAssignmentId() != null) {
            AssignmentEntity assignmentEntity = new AssignmentEntity();
            assignmentEntity.setId(dto.getAssignmentId());
            entity.setAssignment(assignmentEntity);
        }

        return entity;
    }

    @Mapping(target = "id", ignore = true)
    default void updateSubmissionFromDto(AssignmentSubmissionsDTO dto, @MappingTarget AssignmentSubmissionsEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getUserId() != null) {
            entity.setUserId(dto.getUserId());
        }
        if (dto.getNumfiles() != null) {
            entity.setNumfiles(dto.getNumfiles());
        }
        if (dto.getData1() != null) {
            entity.setData1(dto.getData1());
        }
        if (dto.getData2() != null) {
            entity.setData2(dto.getData2());
        }
        if (dto.getGrade() != null) {
            entity.setGrade(dto.getGrade());
        }
        if (dto.getSubmissionComment() != null) {
            entity.setSubmissionComment(dto.getSubmissionComment());
        }
        entity.setUpdatedTime(new java.util.Date());

        // Update assignment reference if assignmentId is provided
        if (dto.getAssignmentId() != null && (entity.getAssignment() == null || !dto.getAssignmentId().equals(entity.getAssignment().getId()))) {
            AssignmentEntity assignmentEntity = new AssignmentEntity();
            assignmentEntity.setId(dto.getAssignmentId());
            entity.setAssignment(assignmentEntity);
        }
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<AssignmentSubmissionsDTO> toDtoList(List<AssignmentSubmissionsEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<AssignmentSubmissionsDTO> toDtoListWithoutContext(List<AssignmentSubmissionsEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }
}