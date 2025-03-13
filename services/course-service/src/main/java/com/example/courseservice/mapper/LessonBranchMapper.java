package com.example.courseservice.mapper;

import com.example.courseservice.dto.LessonBranchDTO;
import com.example.courseservice.entity.LessonBranchEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LessonPagesMapper.class})
public interface LessonBranchMapper {

    @Mapping(target = "lessonPages", source = "lessonPages")
    LessonBranchDTO toDto(LessonBranchEntity entity);

    // Remove this method as it's already defined in LessonPagesMapper
    // default Long map(CourseEntity value) {
    //     return value == null ? null : value.getId();
    // }

    @Mapping(target = "lessonPages", ignore = true)
    default LessonBranchEntity toEntity(LessonBranchDTO dto) {
        if (dto == null) {
            return null;
        }

        LessonBranchEntity entity = new LessonBranchEntity();
        entity.setId(dto.getId());
        entity.setLessonId(dto.getLessonId());
        entity.setUserId(dto.getUserId());
        entity.setTimeSeen(dto.getTimeSeen());

        // Set lessonPages if available in DTO
        if (dto.getLessonPages() != null && dto.getLessonPages().getId() != null) {
            LessonPagesEntity lessonPages = new LessonPagesEntity();
            lessonPages.setId(dto.getLessonPages().getId());
            entity.setLessonPages(lessonPages);
        }

        return entity;
    }

    List<LessonBranchDTO> toDtoList(List<LessonBranchEntity> entityList);

    List<LessonBranchEntity> toEntityList(List<LessonBranchDTO> dtoList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonPages", ignore = true)
    default void updateEntityFromDto(LessonBranchDTO dto, @MappingTarget LessonBranchEntity entity) {
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

        // Update lessonPages reference if provided in DTO
        if (dto.getLessonPages() != null && dto.getLessonPages().getId() != null &&
                (entity.getLessonPages() == null || !dto.getLessonPages().getId().equals(entity.getLessonPages().getId()))) {
            LessonPagesEntity lessonPages = new LessonPagesEntity();
            lessonPages.setId(dto.getLessonPages().getId());
            entity.setLessonPages(lessonPages);
        }
    }
}