package org.example.quizservice.mapper;

import org.example.quizservice.dto.ExamDTO;
import org.example.quizservice.entity.ExamEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ExamQuestionMapper.class})
public interface ExamMapper extends EntityMapper<ExamDTO, ExamEntity> {

    @Override
    @Mapping(target = "examQuestions", ignore = true)
    ExamEntity toEntity(ExamDTO dto);

    @Override
    @Mapping(source = "examQuestions", target = "questions")
    ExamDTO toDto(ExamEntity entity);
}
