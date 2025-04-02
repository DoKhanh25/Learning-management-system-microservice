package org.example.quizservice.mapper;

import org.example.quizservice.dto.QuestionBankDTO;
import org.example.quizservice.entity.QuestionBankEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuestionBankMapper extends EntityMapper<QuestionBankDTO, QuestionBankEntity> {

    @Override
    @Mapping(target = "questions", ignore = true)
    QuestionBankDTO toDto(QuestionBankEntity entity);

    @Override
    @Mapping(target = "questions", ignore = true)
    QuestionBankEntity toEntity(QuestionBankDTO dto);
}
