package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonTimerDTO;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonTimerEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LessonMapper.class})
public interface LessonTimerMapper {

    @Named("toDto")
    @Mapping(target = "lesson", source = "lesson", qualifiedByName = "simple")
    @Mapping(target = "lessonId", source = "lesson.id")
    LessonTimerDTO toDto(LessonTimerEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("toDtoWithoutContext")
    default LessonTimerDTO toDtoWithoutContext(LessonTimerEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("simpleLessonTimerDto")
    @Mapping(target = "lesson", ignore = true)
    LessonTimerDTO toSimpleDto(LessonTimerEntity entity);

    @Mapping(target = "lesson", source = "lessonId", qualifiedByName = "mapLessonIdToEntity")
    LessonTimerEntity toEntity(LessonTimerDTO dto, @Context CycleAvoidingMappingContext context);

    @Named("mapLessonIdToEntity")
    default LessonEntity mapLessonIdToEntity(Long lessonId) {
        if (lessonId == null) {
            return null;
        }
        LessonEntity lessonEntity = new LessonEntity();
        lessonEntity.setId(lessonId);
        return lessonEntity;
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<LessonTimerDTO> toDtoList(List<LessonTimerEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<LessonTimerDTO> toDtoListWithoutContext(List<LessonTimerEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    void updateLessonTimerFromDto(LessonTimerDTO dto, @MappingTarget LessonTimerEntity entity);

    default void updateLessonReference(@MappingTarget LessonTimerEntity entity, Long lessonId) {
        if (lessonId != null) {
            LessonEntity lesson = new LessonEntity();
            lesson.setId(lessonId);
            entity.setLesson(lesson);
        }
    }
}