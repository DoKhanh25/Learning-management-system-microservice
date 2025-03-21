package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonNoteDTO;
import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.entity.LessonNoteEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LessonPagesMapper.class})
public interface LessonNoteMapper {

    @Named("toDto")
    @Mapping(target = "lessonPagesId", source = "lessonPages.id")
    @Mapping(target = "lessonPages", expression = "java(mapLessonPages(entity.getLessonPages(), context))")
    LessonNoteDTO toDto(LessonNoteEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "lessonPagesId", source = "lessonPages.id")
    @Mapping(target = "lessonPages", ignore = true)
    LessonNoteDTO toSimpleDto(LessonNoteEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("toDtoWithoutContext")
    default LessonNoteDTO toDtoWithoutContext(LessonNoteEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "lessonPages", source = "lessonPagesId", qualifiedByName = "mapToLessonPagesEntity")
    LessonNoteEntity toEntity(LessonNoteDTO dto, @Context CycleAvoidingMappingContext context);

    @Named("mapToLessonPagesEntity")
    default LessonPagesEntity mapToLessonPagesEntity(Long id) {
        if (id == null) {
            return null;
        }
        LessonPagesEntity entity = new LessonPagesEntity();
        entity.setId(id);
        // Don't set lessonNotes here to avoid circular reference
        return entity;
    }

    // Helper method to create simplified LessonPagesDTO
    default LessonPagesDTO mapLessonPages(LessonPagesEntity entity, CycleAvoidingMappingContext context) {
        if (entity == null) {
            return null;
        }

        // Check if we've already mapped this entity to avoid cycles
        if (context.getMappedInstance(entity, LessonPagesDTO.class) != null) {
            return context.getMappedInstance(entity, LessonPagesDTO.class);
        }

        LessonPagesDTO dto = new LessonPagesDTO();
        context.storeMappedInstance(entity, dto);

        dto.setId(entity.getId());
        if (entity.getLesson() != null) {
            dto.setLessonId(entity.getLesson().getId());
        }
        dto.setPosition(entity.getPosition());
        dto.setQType(String.valueOf(entity.getQType()));
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setUpdatedTime(entity.getUpdatedTime());

        // Don't map lessonNotes to avoid circular reference

        return dto;
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<LessonNoteDTO> toDtoList(List<LessonNoteEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<LessonNoteDTO> toDtoListWithoutContext(List<LessonNoteEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonPages", source = "lessonPagesId", qualifiedByName = "mapToLessonPagesEntity")
    void updateEntityFromDto(LessonNoteDTO dto, @MappingTarget LessonNoteEntity entity);

    // Add a method to properly handle the bidirectional relationship
    @AfterMapping
    default void handleBidirectionalMapping(@MappingTarget LessonNoteEntity entity) {
        if (entity.getLessonPages() != null) {
            entity.getLessonPages().setLessonNotes(entity);
        }
    }
}