package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.EnrolDTO;
import com.example.courseservice.dto.UserEnrolmentsDTO;
import com.example.courseservice.entity.EnrolEntity;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrolMapper {
    // Ánh xạ đầy đủ EnrolEntity -> EnrolDTO (có userEnrolments)
    @Mapping(target = "userEnrolments", qualifiedByName = "mapMembers")
    @Mapping(target = "course", source = "course.id")
    EnrolDTO toDto(EnrolEntity enrolEntity, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    // Ánh xạ đơn giản EnrolEntity -> EnrolDTO (không có userEnrolments)
    @Mapping(target = "userEnrolments", ignore = true)
    @Mapping(target = "course", source = "course.id")
    @Named("simple")
    EnrolDTO toSimpleDto(EnrolEntity enrolEntity, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    // Ánh xạ UserEnrolmentsEntity -> UserEnrolmentsDTO (dùng phiên bản đơn giản cho enrol)
    @Mapping(target = "enrol", source = "enrol", qualifiedByName = "simple")
    UserEnrolmentsDTO toDto(UserEnrolmentsEntity member, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    @Mapping(target = "course", ignore = true)
    EnrolEntity toEntity(EnrolDTO enrolDTO, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

    // mapMembers dùng toDto của UserEnrolmentsDTO
    @Named("mapMembers")
    default List<UserEnrolmentsDTO> mapMembers(List<UserEnrolmentsEntity> members,
                                               @Context CycleAvoidingMappingContext cycleAvoidingMappingContext) {
        if (members == null) return null;
        return members.stream()
                .map(member -> toDto(member, cycleAvoidingMappingContext))
                .toList();
    }
}

