package org.example.quizservice.mapper;

import org.example.quizservice.dto.ExamQuestionDTO;
import org.example.quizservice.entity.ExamQuestionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamQuestionMapper extends EntityMapper<ExamQuestionDTO, ExamQuestionEntity> {

    @Override
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "question", ignore = true)
    ExamQuestionEntity toEntity(ExamQuestionDTO dto);

    @Override
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(target = "question", ignore = true)
    ExamQuestionDTO toDto(ExamQuestionEntity entity);
}
