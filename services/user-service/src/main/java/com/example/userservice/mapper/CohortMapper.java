package com.example.userservice.mapper;

import com.example.commondto.dto.CohortDTO;
import com.example.commondto.dto.CohortMemberDTO;
import com.example.userservice.entity.CohortEntity;
import com.example.userservice.entity.CohortMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface CohortMapper {
    @Mapping(target = "cohortMembers", qualifiedByName = "mapMembers")
    CohortDTO toDto(CohortEntity cohortEntity);

    CohortEntity toEntity(CohortDTO cohortDTO);

    @Mapping(target = "cohort", ignore = true) // Bỏ qua trường cohort
    CohortMemberDTO toDto(CohortMemberEntity member);

    @Named("mapMembers")
    default List<CohortMemberDTO> mapMembers(List<CohortMemberEntity> members) {
        if (members == null) return null;
        return members.stream()
                .map(this::toDto)
                .toList();
    }

//    @Named("mapCohort")
//    default CohortDTO mapCohort(CohortEntity cohort) {
//        return cohort == null ? null : toDto(cohort);
//    }
}
