package org.example.quizservice.mapper;

import org.example.quizservice.dto.CodingSubmissionDTO;
import org.example.quizservice.entity.CodingSubmissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CodingSubmissionMapper extends EntityMapper<CodingSubmissionDTO, CodingSubmissionEntity> {

    @Override
    @Mapping(target = "examSubmission", ignore = true)
    @Mapping(target = "question", ignore = true)
    CodingSubmissionEntity toEntity(CodingSubmissionDTO dto);

    @Override
    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
    @Mapping(source = "question.id", target = "questionId")
    CodingSubmissionDTO toDto(CodingSubmissionEntity entity);
}
