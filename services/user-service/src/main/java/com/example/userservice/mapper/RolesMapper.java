package com.example.userservice.mapper;

import com.example.userservice.dto.RolesGetDTO;
import org.keycloak.representations.idm.RoleRepresentation;


public class RolesMapper {
    public static RolesGetDTO toRolesGetDTO(RoleRepresentation roleRepresentation){
        RolesGetDTO rolesGetDTO = new RolesGetDTO();
        rolesGetDTO.setId(roleRepresentation.getId());
        rolesGetDTO.setName(roleRepresentation.getName());
        rolesGetDTO.setDescription(roleRepresentation.getDescription());
        rolesGetDTO.setIsComposite(roleRepresentation.isComposite());
        return rolesGetDTO;
    }
}
