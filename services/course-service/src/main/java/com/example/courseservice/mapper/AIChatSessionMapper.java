package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AIChatSessionDTO;
import com.example.courseservice.dto.LessonDTO;
import com.example.courseservice.entity.AIChatSessionEntity;
import com.example.courseservice.entity.LessonEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AIChatMapper.class})
public interface AIChatSessionMapper {
    LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);

    @Named("toDto")
    @Mapping(target = "messages", source = "messages", qualifiedByName = "toDtoList")
    @Mapping(target = "lesson", source = "lesson", qualifiedByName = "mapLesson")
    @Mapping(target = "lessonId", source = "lesson.id")
    AIChatSessionDTO toDto(AIChatSessionEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "lessonId", source = "lesson.id")
    AIChatSessionDTO toSimpleDto(AIChatSessionEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("toDtoWithoutContext")
    default AIChatSessionDTO toDtoWithoutContext(AIChatSessionEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    default AIChatSessionEntity toEntity(AIChatSessionDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        AIChatSessionEntity entity = new AIChatSessionEntity();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setContextUsed(dto.getContextUsed());
        entity.setSessionName(dto.getSessionName());
        entity.setCreatedTime(dto.getCreatedTime());
        entity.setUpdatedTime(dto.getUpdatedTime());

        // Set lesson by ID if provided
        if (dto.getLessonId() != null) {
            LessonEntity lesson = new LessonEntity();
            lesson.setId(dto.getLessonId());
            entity.setLesson(lesson);
        }

        return entity;
    }


    @Named("mapLesson")
    default LessonDTO mapLesson(LessonEntity entity) {
        if (entity == null) {
            return null;
        }
        return lessonMapper.toSimpleDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<AIChatSessionDTO> toDtoList(List<AIChatSessionEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<AIChatSessionDTO> toDtoListWithoutContext(List<AIChatSessionEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    default void updateSessionFromDto(AIChatSessionDTO dto, @MappingTarget AIChatSessionEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getUserId() != null) {
            entity.setUserId(dto.getUserId());
        }
        if (dto.getContextUsed() != null) {
            entity.setContextUsed(dto.getContextUsed());
        }
        if (dto.getSessionName() != null) {
            entity.setSessionName(dto.getSessionName());
        }
        entity.setUpdatedTime(new Date());

        // Update lesson reference if lessonId is provided
        if (dto.getLessonId() != null && (entity.getLesson() == null || !dto.getLessonId().equals(entity.getLesson().getId()))) {
            LessonEntity lesson = new LessonEntity();
            lesson.setId(dto.getLessonId());
            entity.setLesson(lesson);
        }
    }
}