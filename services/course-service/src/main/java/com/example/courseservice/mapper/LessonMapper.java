package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonDTO;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.LessonEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CourseSectionsMapper.class})
public interface LessonMapper {

    @Named("toDto")
    @Mapping(target = "section", source = "section", qualifiedByName = "simple")
    @Mapping(target = "sectionId", source = "section.id")
    LessonDTO toDto(LessonEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "lessonPages", ignore = true)
    @Mapping(target = "lessonTimers", ignore = true)
    LessonDTO toSimpleDto(LessonEntity entity, @Context CycleAvoidingMappingContext context);

    default Long map(CourseSectionsEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default LessonDTO toDtoWithoutContext(LessonEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "lessonPages", ignore = true)
    @Mapping(target = "lessonTimers", ignore = true)
    default LessonEntity toEntity(LessonDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        LessonEntity entity = new LessonEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setIntro(dto.getIntro());
        entity.setCreatedTime(dto.getCreatedTime());
        entity.setUpdatedTime(dto.getUpdatedTime());

        // Set section by ID if provided
        if (dto.getSectionId() != null) {
            CourseSectionsEntity section = new CourseSectionsEntity();
            section.setId(dto.getSectionId());
            entity.setSection(section);
        }

        return entity;
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<LessonDTO> toDtoList(List<LessonEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<LessonDTO> toDtoListWithoutContext(List<LessonEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonPages", ignore = true)
    @Mapping(target = "lessonTimers", ignore = true)
    default void updateLessonFromDto(LessonDTO dto, @MappingTarget LessonEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getIntro() != null) {
            entity.setIntro(dto.getIntro());
        }
        entity.setUpdatedTime(new java.util.Date());

        // Update section reference if sectionId is provided
        if (dto.getSectionId() != null && (entity.getSection() == null || !dto.getSectionId().equals(entity.getSection().getId()))) {
            CourseSectionsEntity section = new CourseSectionsEntity();
            section.setId(dto.getSectionId());
            entity.setSection(section);
        }
    }
}