//package org.example.quizservice.mapper;
//
//import org.example.quizservice.dto.CodingQuestionDTO;
//import org.example.quizservice.dto.EssayQuestionDTO;
//import org.example.quizservice.dto.MultipleChoiceQuestionDTO;
//import org.example.quizservice.dto.QuestionDTO;
//import org.example.quizservice.entity.*;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface QuestionMapper extends EntityMapper<QuestionDTO, QuestionEntity> {
//
//    // Explicit mappings for concrete implementations
//    @Mapping(target = "questionBank", ignore = true)
//    MultipleChoiceQuestionEntity toEntity(MultipleChoiceQuestionDTO dto);
//
//    @Mapping(target = "questionBank", ignore = true)
//    EssayQuestionEntity toEntity(EssayQuestionDTO dto);
//
//    @Mapping(target = "questionBank", ignore = true)
//    CodingQuestionEntity toEntity(CodingQuestionDTO dto);
//
//    // Generic method that will delegate to the specific implementations above
//    @Override
//    @Mapping(target = "questionBank", ignore = true)
//    QuestionEntity toEntity(QuestionDTO dto);
//
//    // Reverse mappings
//    @Mapping(source = "questionBank.id", target = "questionBankId")
//    MultipleChoiceQuestionDTO toDto(MultipleChoiceQuestionEntity entity);
//
//    @Mapping(source = "questionBank.id", target = "questionBankId")
//    EssayQuestionDTO toDto(EssayQuestionEntity entity);
//
//    @Mapping(source = "questionBank.id", target = "questionBankId")
//    CodingQuestionDTO toDto(CodingQuestionEntity entity);
//
//    @Override
//    @Mapping(source = "questionBank.id", target = "questionBankId")
//    QuestionDTO toDto(QuestionEntity entity);
//}
