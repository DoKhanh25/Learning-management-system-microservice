package org.example.quizservice.mapper;

import org.example.quizservice.dto.ExamDTO;
import org.example.quizservice.entity.ExamEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ExamQuestionMapper.class, ExamSubmissionMapper.class})
public interface ExamMapper extends EntityMapper<ExamDTO, ExamEntity> {

    @Override
    @Mapping(target = "examQuestions", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    ExamEntity toEntity(ExamDTO dto);

    @Override
    @Mapping(source = "examQuestions", target = "questions")
    @Mapping(source = "submissions", target = "submissions")
    ExamDTO toDto(ExamEntity entity);
}
