package org.example.quizservice.mapper;

import org.example.quizservice.dto.MultipleChoiceOptionDTO;
import org.example.quizservice.entity.MultipleChoiceOptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MultipleChoiceOptionMapper extends EntityMapper<MultipleChoiceOptionDTO, MultipleChoiceOptionEntity> {

    @Override
    @Mapping(target = "question", ignore = true)
    MultipleChoiceOptionEntity toEntity(MultipleChoiceOptionDTO dto);

    @Override
    @Mapping(source = "question.id", target = "questionId")
    MultipleChoiceOptionDTO toDto(MultipleChoiceOptionEntity entity);
}
