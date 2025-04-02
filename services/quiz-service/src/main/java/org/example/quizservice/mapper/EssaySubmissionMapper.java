package org.example.quizservice.mapper;

import org.example.quizservice.dto.EssaySubmissionDTO;
import org.example.quizservice.entity.EssaySubmissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EssaySubmissionMapper extends EntityMapper<EssaySubmissionDTO, EssaySubmissionEntity> {

    @Override
    @Mapping(target = "examSubmission", ignore = true)
    @Mapping(target = "question", ignore = true)
    EssaySubmissionEntity toEntity(EssaySubmissionDTO dto);

    @Override
    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
    @Mapping(source = "question.id", target = "questionId")
    EssaySubmissionDTO toDto(EssaySubmissionEntity entity);
}
