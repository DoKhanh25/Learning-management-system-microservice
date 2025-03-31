package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonBranchDTO;
import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.entity.LessonBranchEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LessonBranchMapper {

    @Named("toDto")
    @Mapping(target = "lessonPages", expression = "java(createSimpleLessonPagesDto(entity.getLessonPages()))")
    @Mapping(target = "lessonPagesId", source = "lessonPages.id")
    LessonBranchDTO toDto(LessonBranchEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("toDtoWithoutContext")
    default LessonBranchDTO toDtoWithoutContext(LessonBranchEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("simpleDto")
    @Mapping(target = "lessonPages", ignore = true)
    @Mapping(target = "lessonPagesId", source = "lessonPages.id")
    LessonBranchDTO toSimpleDto(LessonBranchEntity entity);

    // Create a simple DTO with only ID to avoid circular references
    default LessonPagesDTO createSimpleLessonPagesDto(LessonPagesEntity entity) {
        if (entity == null) {
            return null;
        }
        LessonPagesDTO dto = new LessonPagesDTO();
        dto.setId(entity.getId());
        return dto;
    }

    @Named("toEntity")
    @Mapping(target = "lessonPages", source = "lessonPagesId", qualifiedByName = "mapLessonPagesIdToEntity")
    LessonBranchEntity toEntity(LessonBranchDTO dto, @Context CycleAvoidingMappingContext context);

    @Named("mapLessonPagesIdToEntity")
    default LessonPagesEntity mapLessonPagesIdToEntity(Long lessonPagesId) {
        if (lessonPagesId == null) {
            return null;
        }
        LessonPagesEntity pagesEntity = new LessonPagesEntity();
        pagesEntity.setId(lessonPagesId);
        return pagesEntity;
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<LessonBranchDTO> toDtoList(List<LessonBranchEntity> entityList, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<LessonBranchDTO> toDtoListWithoutContext(List<LessonBranchEntity> entityList) {
        return entityList == null ? null : toDtoList(entityList, new CycleAvoidingMappingContext());
    }

    @Named("toEntityList")
    @IterableMapping(qualifiedByName = "toEntity")
    List<LessonBranchEntity> toEntityList(List<LessonBranchDTO> dtoList, @Context CycleAvoidingMappingContext context);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonPages", ignore = true)
    void updateEntityFromDto(LessonBranchDTO dto, @MappingTarget LessonBranchEntity entity);

    default void updateLessonPagesReference(@MappingTarget LessonBranchEntity entity, Long lessonPagesId) {
        if (lessonPagesId != null) {
            LessonPagesEntity lessonPages = new LessonPagesEntity();
            lessonPages.setId(lessonPagesId);
            entity.setLessonPages(lessonPages);
        }
    }
}