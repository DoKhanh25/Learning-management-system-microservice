package com.example.userservice.mapper;

import com.example.userservice.dto.UserInfoGetDTO;
import org.keycloak.representations.idm.UserRepresentation;

public class UserInfoMapper {
    public static UserInfoGetDTO toUserDTO(UserRepresentation userRepresentation){
        UserInfoGetDTO userInfoGetDTO = new UserInfoGetDTO();
        userInfoGetDTO.setUserId(userRepresentation.getId());
        userInfoGetDTO.setEmail(userRepresentation.getEmail());
        userInfoGetDTO.setEnable(userRepresentation.isEnabled());
        userInfoGetDTO.setLastName(userRepresentation.getLastName());
        userInfoGetDTO.setFirstName(userRepresentation.getFirstName());
        userInfoGetDTO.setAttributes(userRepresentation.getAttributes());
        userInfoGetDTO.setGroups(userRepresentation.getGroups());
        userInfoGetDTO.setUsername(userRepresentation.getUsername());
        return userInfoGetDTO;
    }

    public static UserRepresentation toUserRepresentation(UserInfoGetDTO userInfoGetDTO){
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(userInfoGetDTO.getEmail());
        userRepresentation.setEnabled(userInfoGetDTO.getEnable());
        userRepresentation.setLastName(userInfoGetDTO.getLastName());
        userRepresentation.setFirstName(userInfoGetDTO.getFirstName());
        userRepresentation.setGroups(userInfoGetDTO.getGroups());
        userRepresentation.setAttributes(userInfoGetDTO.getAttributes());
        userRepresentation.setUsername(userInfoGetDTO.getUsername());

        return userRepresentation;
    }

}
