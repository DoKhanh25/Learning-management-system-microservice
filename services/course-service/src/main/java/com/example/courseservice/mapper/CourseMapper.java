package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.CourseDTO;
import com.example.courseservice.dto.CourseSectionsDTO;
import com.example.courseservice.entity.CourseEntity;
import com.example.courseservice.entity.CourseSectionsEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    @Named("toDto")
    @Mapping(target = "courseSections", source = "courseSections", qualifiedByName = "mapCourseSections")
    @Mapping(target = "enrols", ignore = true)
    @Mapping(target = "resources", ignore = true)
    CourseDTO toDto(CourseEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "courseSections", ignore = true)
    @Mapping(target = "enrols", ignore = true)
    @Mapping(target = "resources", ignore = true)
    CourseDTO toSimpleDto(CourseEntity entity, @Context CycleAvoidingMappingContext context);

    default Long map(CourseEntity value) {
        return value == null ? null : value.getId();
    }

    @Named("toDtoWithoutContext")
    default CourseDTO toDtoWithoutContext(CourseEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "courseSections", ignore = true)
    @Mapping(target = "enrols", ignore = true)
    @Mapping(target = "resources", ignore = true)
    CourseEntity toEntity(CourseDTO dto, @Context CycleAvoidingMappingContext context);

    @Named("mapCourseSections")
    default List<CourseSectionsDTO> mapCourseSections(List<CourseSectionsEntity> sections,
                                                      @Context CycleAvoidingMappingContext context) {
        if (sections == null) return null;

        if (context.getMappedInstance(sections, List.class) != null) {
            return (List<CourseSectionsDTO>) context.getMappedInstance(sections, List.class);
        }

        List<CourseSectionsDTO> result = sections.stream()
                .map(section -> mapCourseSection(section, context))
                .toList();
        context.storeMappedInstance(sections, result);
        return result;
    }

    @Named("mapCourseSection")
    @Mapping(target = "course", ignore = true) // Break circular reference
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessons", ignore = true) // Avoid deep nesting issues
    CourseSectionsDTO mapCourseSection(CourseSectionsEntity section, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courseSections", ignore = true)
    @Mapping(target = "enrols", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateCourseFromDto(CourseDTO dto, @MappingTarget CourseEntity entity);

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<CourseDTO> toDtoList(List<CourseEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<CourseDTO> toDtoListWithoutContext(List<CourseEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }
}