package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.dto.LessonDTO;
import com.example.courseservice.entity.CourseSectionsEntity;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.LessonEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CourseMapper.class})
public interface CourseSectionsMapper {

    @Named("toDto")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "course", source = "course", qualifiedByName = "simple")
    @Mapping(target = "lessons", source = "lessons", qualifiedByName = "mapLessons")
    CourseSectionsDTO toDto(CourseSectionsEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    CourseSectionsDTO toSimpleDto(CourseSectionsEntity entity, @Context CycleAvoidingMappingContext context);

    default Long map(CourseSectionsEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default CourseSectionsDTO toDtoWithoutContext(CourseSectionsEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "lessons", ignore = true)
    default CourseSectionsEntity toEntity(CourseSectionsDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        CourseSectionsEntity entity = new CourseSectionsEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setSummary(dto.getSummary());
        entity.setSection(dto.getSection());
        entity.setCreatedTime(dto.getCreatedTime());
        entity.setUpdatedTime(dto.getUpdatedTime());

        // Handle course reference by ID
        if (dto.getCourseId() != null) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setId(dto.getCourseId());
            entity.setCourse(courseEntity);
        }

        return entity;
    }

    @Named("mapLessons")
    default List<LessonDTO> mapLessons(List<LessonEntity> lessons, @Context CycleAvoidingMappingContext context) {
        if (lessons == null) return null;

        if (context.getMappedInstance(lessons, List.class) != null) {
            return (List<LessonDTO>) context.getMappedInstance(lessons, List.class);
        }

        List<LessonDTO> result = lessons.stream()
                .map(lesson -> mapLesson(lesson, context))
                .toList();
        context.storeMappedInstance(lessons, result);
        return result;
    }

    @Mapping(target = "section", ignore = true) // Avoid circular reference
    LessonDTO mapLesson(LessonEntity lesson, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    default void updateCourseSectionFromDto(CourseSectionsDTO dto, @MappingTarget CourseSectionsEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getSummary() != null) {
            entity.setSummary(dto.getSummary());
        }
        if (dto.getSection() != null) {
            entity.setSection(dto.getSection());
        }
        entity.setUpdatedTime(new java.util.Date());

        // Update course reference if courseId is provided
        if (dto.getCourseId() != null && (entity.getCourse() == null || !dto.getCourseId().equals(entity.getCourse().getId()))) {
            CourseEntity courseEntity = new CourseEntity();
            courseEntity.setId(dto.getCourseId());
            entity.setCourse(courseEntity);
        }
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<CourseSectionsDTO> toDtoList(List<CourseSectionsEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<CourseSectionsDTO> toDtoListWithoutContext(List<CourseSectionsEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }
}