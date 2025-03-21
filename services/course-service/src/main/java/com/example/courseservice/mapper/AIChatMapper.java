package com.example.courseservice.mapper;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AIChatDTO;
import com.example.courseservice.dto.AIChatSessionDTO;
import com.example.courseservice.entity.AIChatEntity;
import com.example.courseservice.entity.AIChatSessionEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AIChatMapper {

    // Update your toDto method to use this method
    @Named("toDto")
    @Mapping(target = "session", source = "session", qualifiedByName = "mapSession")
    @Mapping(target = "sessionId", source = "session.id")
    AIChatDTO toDto(AIChatEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("simple")
    @Mapping(target = "session", ignore = true)
    AIChatDTO toSimpleDto(AIChatEntity entity, @Context CycleAvoidingMappingContext context);

    @Named("toDtoWithoutContext")
    default AIChatDTO toDtoWithoutContext(AIChatEntity entity) {
        return entity == null ? null : toDto(entity, new CycleAvoidingMappingContext());
    }

    @Named("mapSession")
    default AIChatSessionDTO mapSession(AIChatSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        AIChatSessionDTO dto = new AIChatSessionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setContextUsed(entity.getContextUsed());
        dto.setSessionName(entity.getSessionName());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setUpdatedTime(entity.getUpdatedTime());

        if (entity.getLesson() != null) {
            dto.setLessonId(entity.getLesson().getId());
        }

        return dto;
    }

    @Mapping(target = "session", ignore = true)
    default AIChatEntity toEntity(AIChatDTO dto, @Context CycleAvoidingMappingContext context) {
        if (dto == null) {
            return null;
        }

        AIChatEntity entity = new AIChatEntity();
        entity.setId(dto.getId());
        entity.setUserMessage(dto.getUserMessage());
        entity.setGeminiResponse(dto.getGeminiResponse());
        entity.setMessageOrder(dto.getMessageOrder());
        entity.setCreatedTime(dto.getCreatedTime());

        // Set session if needed and available
        if (dto.getSession() != null && dto.getSession().getId() != null) {
            AIChatSessionEntity session = new AIChatSessionEntity();
            session.setId(dto.getSession().getId());
            entity.setSession(session);
        }

        return entity;
    }

    @Named("toDtoList")
    @IterableMapping(qualifiedByName = "toDto")
    List<AIChatDTO> toDtoList(List<AIChatEntity> entities, @Context CycleAvoidingMappingContext context);

    @Named("toDtoListWithoutContext")
    default List<AIChatDTO> toDtoListWithoutContext(List<AIChatEntity> entities) {
        return entities == null ? null : toDtoList(entities, new CycleAvoidingMappingContext());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    default void updateChatFromDto(AIChatDTO dto, @MappingTarget AIChatEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getUserMessage() != null) {
            entity.setUserMessage(dto.getUserMessage());
        }
        if (dto.getGeminiResponse() != null) {
            entity.setGeminiResponse(dto.getGeminiResponse());
        }
        if (dto.getMessageOrder() != null) {
            entity.setMessageOrder(dto.getMessageOrder());
        }
    }
}