package com.example.userservice.mapper;

import com.example.userservice.dto.CompositesDTO;
import org.keycloak.representations.idm.RoleRepresentation;

public class CompositeMapper {
    public static CompositesDTO toCompositesDTO(RoleRepresentation.Composites composites){
        if(composites != null){
            CompositesDTO compositesDTO = new CompositesDTO();
            compositesDTO.setClient(composites.getClient());
            compositesDTO.setRealm(composites.getRealm());
            return compositesDTO;
        }
        return null;
    }

    public static RoleRepresentation.Composites toComposites(CompositesDTO compositesDTO){
        RoleRepresentation.Composites composites = new RoleRepresentation.Composites();
        composites.setClient(compositesDTO.getClient());
        composites.setRealm(compositesDTO.getRealm());
        return composites;
    }
}
