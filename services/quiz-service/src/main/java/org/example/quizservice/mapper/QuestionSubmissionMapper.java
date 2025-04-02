//package org.example.quizservice.mapper;
//
//import org.example.quizservice.dto.CodingSubmissionDTO;
//import org.example.quizservice.dto.EssaySubmissionDTO;
//import org.example.quizservice.dto.MultipleChoiceSubmissionDTO;
//import org.example.quizservice.dto.QuestionSubmissionDTO;
//import org.example.quizservice.entity.*;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.SubclassMapping;
//
//@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
//public interface QuestionSubmissionMapper extends EntityMapper<QuestionSubmissionDTO, QuestionSubmissionEntity> {
//
//    // Explicit mappings for concrete implementations
//    @Mapping(target = "examSubmission", ignore = true)
//    @Mapping(target = "question", ignore = true)
//    MultipleChoiceSubmissionEntity toEntity(MultipleChoiceSubmissionDTO dto);
//
//    @Mapping(target = "examSubmission", ignore = true)
//    @Mapping(target = "question", ignore = true)
//    EssaySubmissionEntity toEntity(EssaySubmissionDTO dto);
//
//    @SubclassMapping(target = EssaySubmissionEntity.class, source = EssaySubmissionDTO.class)
//    @SubclassMapping(target = CodingSubmissionEntity.class, source = CodingSubmissionDTO.class)
//    @SubclassMapping(target = MultipleChoiceSubmissionEntity.class, source = MultipleChoiceSubmissionDTO.class)
//    CodingSubmissionEntity toEntity(CodingSubmissionDTO dto);
//
//    // Generic method that will delegate to the specific implementations above
//    @Override
//    @Mapping(target = "examSubmission", ignore = true)
//    @Mapping(target = "question", ignore = true)
//    QuestionSubmissionEntity toEntity(QuestionSubmissionDTO dto);
//
//    // Reverse mappings
//    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
//    @Mapping(source = "question.id", target = "questionId")
//    MultipleChoiceSubmissionDTO toDto(MultipleChoiceSubmissionEntity entity);
//
//    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
//    @Mapping(source = "question.id", target = "questionId")
//    EssaySubmissionDTO toDto(EssaySubmissionEntity entity);
//
//    @Mapping(source = "examSubmission.id", target = "examSubmissionId")
//    @Mapping(source = "question.id", target = "questionId")
//    CodingSubmissionDTO toDto(CodingSubmissionEntity entity);
//
//    @Override
//    @SubclassMapping(target = EssaySubmissionDTO.class, source = EssaySubmissionEntity.class)
//    @SubclassMapping(target = CodingSubmissionDTO.class, source = CodingSubmissionEntity.class)
//    @SubclassMapping(target = MultipleChoiceSubmissionDTO.class, source = MultipleChoiceSubmissionEntity.class)
//    QuestionSubmissionDTO toDto(QuestionSubmissionEntity entity);
//}