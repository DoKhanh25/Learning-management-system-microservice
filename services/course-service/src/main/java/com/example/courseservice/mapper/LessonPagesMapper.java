package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.enums.QType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LessonMapper.class})
public interface LessonPagesMapper {

    @Named("toDto")
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lesson", source = "lesson", qualifiedByName = "simple")
    @Mapping(target = "lessonAttempts", ignore = true)
    @Mapping(target = "lessonBranch", ignore = true)
    LessonPagesDTO toDto(LessonPagesEntity entity, @Context CycleAvoidingMappingContext context);

    // This method is used by other mappers - keep it
    default Long map(CourseEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default LessonPagesDTO toDtoWithoutContext(LessonPagesEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("toEntity")
    @Mapping(target = "lessonAttempts", ignore = true)
    @Mapping(target = "lessonBranch", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    default LessonPagesEntity toEntity(LessonPagesDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        LessonPagesEntity entity = new LessonPagesEntity();
        entity.setId(dto.getId());
        entity.setPosition(dto.getPosition());
        entity.setQType(QType.valueOf(dto.getQType()));
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setCreatedTime(dto.getCreatedTime());
        entity.setUpdatedTime(dto.getUpdatedTime());

        // Handle lesson reference by ID
        if (dto.getLessonId() != null) {
            LessonEntity lessonEntity = new LessonEntity();
            lessonEntity.setId(dto.getLessonId());
            entity.setLesson(lessonEntity);
        }

        return entity;
    }

    default LessonPagesEntity toEntity(LessonPagesDTO dto) {
        return dto == null ? null : toEntity(dto, new CycleAvoidingMappingContext());
    }

    @IterableMapping(qualifiedByName = "toDto")
    List<LessonPagesDTO> toDtoList(List<LessonPagesEntity> entityList, @Context CycleAvoidingMappingContext context);

    default List<LessonPagesDTO> toDtoList(List<LessonPagesEntity> entityList) {
        return entityList == null ? null : toDtoList(entityList, new CycleAvoidingMappingContext());
    }

    @IterableMapping(qualifiedByName = "toEntity")
    List<LessonPagesEntity> toEntityList(List<LessonPagesDTO> dtoList, @Context CycleAvoidingMappingContext context);

    default List<LessonPagesEntity> toEntityList(List<LessonPagesDTO> dtoList) {
        return dtoList == null ? null : toEntityList(dtoList, new CycleAvoidingMappingContext());
    }

    @Named("updateEntityFromDto")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "lessonAttempts", ignore = true)
    @Mapping(target = "lessonBranch", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateEntityFromDto(LessonPagesDTO dto, @MappingTarget LessonPagesEntity entity);
}