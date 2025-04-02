package org.example.quizservice.mapper;

import org.example.quizservice.dto.MultipleChoiceQuestionDTO;
import org.example.quizservice.entity.MultipleChoiceQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MultipleChoiceOptionMapper.class})
public interface MultipleChoiceQuestionMapper extends EntityMapper<MultipleChoiceQuestionDTO, MultipleChoiceQuestionEntity> {

    @Override
    @Mapping(target = "questionBank", ignore = true)
    @Mapping(target = "options", source = "options")
    MultipleChoiceQuestionEntity toEntity(MultipleChoiceQuestionDTO dto);

    @Override
    @Mapping(source = "questionBank.id", target = "questionBankId")
    @Mapping(target = "options", source = "options")
    MultipleChoiceQuestionDTO toDto(MultipleChoiceQuestionEntity entity);
}
