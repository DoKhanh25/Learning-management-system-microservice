package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonAttemptsDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.LessonAttemptsEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {LessonPagesMapper.class})
public interface LessonAttemptsMapper {

    @Mapping(target = "lessonPagesId", source = "lessonPages.id")
    @Mapping(target = "lessonPages", source = "lessonPages")
    LessonAttemptsDTO toDto(LessonAttemptsEntity entity);

    @Mapping(target = "lessonPages", ignore = true)
    default LessonAttemptsEntity toEntity(LessonAttemptsDTO dto) {
        if (dto == null) {
            return null;
        }

        LessonAttemptsEntity entity = new LessonAttemptsEntity();
        entity.setId(dto.getId());
        entity.setLessonId(dto.getLessonId());
        entity.setUserId(dto.getUserId());
        entity.setTimeSeen(dto.getTimeSeen());
        entity.setCorrect(dto.getCorrect());
        entity.setUserAnswer(dto.getUserAnswer());

        // Handle lessonPages relationship
        if (dto.getLessonPagesId() != null) {
            LessonPagesEntity lessonPages = new LessonPagesEntity();
            lessonPages.setId(dto.getLessonPagesId());
            entity.setLessonPages(lessonPages);
        }

        return entity;
    }

    List<LessonAttemptsDTO> toDtoList(List<LessonAttemptsEntity> entityList);

    List<LessonAttemptsEntity> toEntityList(List<LessonAttemptsDTO> dtoList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonPages", ignore = true)
    default void updateEntityFromDto(LessonAttemptsDTO dto, @MappingTarget LessonAttemptsEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getLessonId() != null) {
            entity.setLessonId(dto.getLessonId());
        }
        if (dto.getUserId() != null) {
            entity.setUserId(dto.getUserId());
        }
        if (dto.getTimeSeen() != null) {
            entity.setTimeSeen(dto.getTimeSeen());
        }
        if (dto.getCorrect() != null) {
            entity.setCorrect(dto.getCorrect());
        }
        if (dto.getUserAnswer() != null) {
            entity.setUserAnswer(dto.getUserAnswer());
        }

        // Update lessonPages reference if lessonPagesId is provided
        if (dto.getLessonPagesId() != null && (entity.getLessonPages() == null ||
                !dto.getLessonPagesId().equals(entity.getLessonPages().getId()))) {
            LessonPagesEntity lessonPages = new LessonPagesEntity();
            lessonPages.setId(dto.getLessonPagesId());
            entity.setLessonPages(lessonPages);
        }
    }
}