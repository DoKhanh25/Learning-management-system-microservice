package com.example.userservice.mapper;

import com.example.userservice.dto.UserSessionGetDTO;
import org.keycloak.representations.idm.UserSessionRepresentation;

import java.util.Date;

public class UserSessionMapper {
    public static UserSessionGetDTO userSessionDTO(UserSessionRepresentation userSessionRepresentation){
        UserSessionGetDTO userSessionGetDTO = new UserSessionGetDTO();
        userSessionGetDTO.setUserId(userSessionRepresentation.getUserId());
        userSessionGetDTO.setUsername(userSessionRepresentation.getUsername());
        userSessionGetDTO.setIpAddress(userSessionRepresentation.getIpAddress());
        userSessionGetDTO.setStart(new Date(userSessionRepresentation.getStart()));
        userSessionGetDTO.setLastAccess(new Date(userSessionRepresentation.getLastAccess()));
        userSessionGetDTO.setClients(userSessionRepresentation.getClients());
        return userSessionGetDTO;
    }

}
