package org.example.quizservice.mapper;

import org.example.quizservice.dto.ExamSubmissionDTO;
import org.example.quizservice.entity.ExamSubmissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamSubmissionMapper extends EntityMapper<ExamSubmissionDTO, ExamSubmissionEntity> {

    @Override
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "questionSubmissions", ignore = true)
    ExamSubmissionEntity toEntity(ExamSubmissionDTO dto);

    @Override
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(target = "questionSubmissions", ignore = true)
    ExamSubmissionDTO toDto(ExamSubmissionEntity entity);
}
