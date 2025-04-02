package org.example.quizservice.mapper;

import org.example.quizservice.dto.CodingQuestionDTO;
import org.example.quizservice.entity.CodingQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CodingQuestionMapper extends EntityMapper<CodingQuestionDTO, CodingQuestionEntity> {

    @Override
    @Mapping(target = "questionBank", ignore = true)
    CodingQuestionEntity toEntity(CodingQuestionDTO dto);

    @Override
    @Mapping(source = "questionBank.id", target = "questionBankId")
    CodingQuestionDTO toDto(CodingQuestionEntity entity);
}
