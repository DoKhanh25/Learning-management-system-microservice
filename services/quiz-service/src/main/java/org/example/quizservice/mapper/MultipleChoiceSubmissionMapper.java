package org.example.quizservice.mapper;

import org.example.quizservice.dto.MultipleChoiceSubmissionDTO;
import org.example.quizservice.entity.MultipleChoiceSubmissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MultipleChoiceSubmissionMapper extends EntityMapper<MultipleChoiceSubmissionDTO, MultipleChoiceSubmissionEntity> {

    @Override
    @Mapping(target = "examSubmission", ignore = true)
    @Mapping(target = "question", ignore = true)
    MultipleChoiceSubmissionEntity toEntity(MultipleChoiceSubmissionDTO dto);

    @Override
    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
    @Mapping(source = "question.id", target = "questionId")
    MultipleChoiceSubmissionDTO toDto(MultipleChoiceSubmissionEntity entity);
}
