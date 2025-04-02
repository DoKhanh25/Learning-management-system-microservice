package org.example.quizservice.mapper;

import org.example.quizservice.dto.EssayQuestionDTO;
import org.example.quizservice.entity.EssayQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EssayQuestionMapper extends EntityMapper<EssayQuestionDTO, EssayQuestionEntity> {

    @Override
    @Mapping(target = "questionBank", ignore = true)
    EssayQuestionEntity toEntity(EssayQuestionDTO dto);

    @Override
    @Mapping(source = "questionBank.id", target = "questionBankId")
    EssayQuestionDTO toDto(EssayQuestionEntity entity);
}
