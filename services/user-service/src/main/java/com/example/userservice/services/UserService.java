package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.configuration.Utils;
import com.example.userservice.dto.UserDTO;
import com.example.userservice.mapper.UserMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@NoArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    KeycloakProvider keycloakProvider;

    @Value("${keycloak.realm}")
    String realm;

    public ResponseEntity<List<UserDTO>> getAllUsers(){
        Keycloak keycloak = keycloakProvider.getInstance();

        List<UserRepresentation> userKeycloakList = keycloak.realm(realm).users().list();
        List<UserDTO> userDTOs = new ArrayList<>();
        for(UserRepresentation userKeycloak: userKeycloakList){
            UserDTO userDTO = UserMapper.toUserDTO(userKeycloak);
            userDTOs.add(userDTO);
        }

        keycloak.close();
        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<List<UserDTO>> searchUserListByIdOrUsernameOrEmail(String id){
        List<UserRepresentation> userRepresentationList;
        List<UserDTO> userDTOs = new ArrayList<>();
        Keycloak keycloak = keycloakProvider.getInstance();

        userRepresentationList = keycloak.realm(realm).users().search(id);
        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            userRepresentationList = keycloak.realm(realm).users().searchByUsername(id, false);
        }

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            userRepresentationList = keycloak.realm(realm).users().searchByEmail(id, false);
        }

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            return ResponseEntity.ok(userDTOs);
        }

        for(UserRepresentation userKeycloak: userRepresentationList){
            UserDTO userDTO = UserMapper.toUserDTO(userKeycloak);
            userDTOs.add(userDTO);
        }

        keycloak.close();
        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<UserDTO> getUserByIdOrUsernameOrEmail(String id){
        UserRepresentation userRepresentation = new UserRepresentation();
        Keycloak keycloak = keycloakProvider.getInstance();
        UserDTO userDTO;

        userRepresentation = keycloak.realm(realm).users().search(id, true).get(0);
        if(userRepresentation == null){
            userRepresentation = keycloak.realm(realm).users().searchByUsername(id, true).get(0);
        }
        if(userRepresentation == null){
            userRepresentation = keycloak.realm(realm).users().searchByEmail(id, true).get(0);
        }

        if(userRepresentation == null){
            return ResponseEntity.ok(new UserDTO());
        }
        userDTO = UserMapper.toUserDTO(userRepresentation);

        keycloak.close();
        return ResponseEntity.ok(userDTO);
    }




}
