package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.configuration.Utils;
import com.example.userservice.dto.UserInfoGetDTO;
import com.example.userservice.dto.UserInfoPostDTO;
import com.example.userservice.dto.UserSessionGetDTO;
import com.example.userservice.exception.KeycloakException;
import com.example.userservice.mapper.UserInfoMapper;
import com.example.userservice.mapper.UserSessionMapper;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
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

    public ResponseEntity<List<UserInfoGetDTO>> getAllUsers(){
        Keycloak keycloak = keycloakProvider.getInstance();

        List<UserRepresentation> userKeycloakList = keycloak.realm(realm).users().list();
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        for(UserRepresentation userKeycloak: userKeycloakList){
            UserInfoGetDTO userInfoGetDTO = UserInfoMapper.toUserDTO(userKeycloak);
            userInfoGetDTOS.add(userInfoGetDTO);
        }

        keycloak.close();
        return ResponseEntity.ok(userInfoGetDTOS);
    }

    public ResponseEntity<List<UserInfoGetDTO>> searchUserListByIdOrUsernameOrEmail(String id){
        List<UserRepresentation> userRepresentationList;
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        Keycloak keycloak = keycloakProvider.getInstance();

        userRepresentationList = keycloak.realm(realm).users().search(id);
        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            userRepresentationList = keycloak.realm(realm).users().searchByUsername(id, false);
        }

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            userRepresentationList = keycloak.realm(realm).users().searchByEmail(id, false);
        }

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            return ResponseEntity.ok(userInfoGetDTOS);
        }

        for(UserRepresentation userKeycloak: userRepresentationList){
            UserInfoGetDTO userInfoGetDTO = UserInfoMapper.toUserDTO(userKeycloak);
            userInfoGetDTOS.add(userInfoGetDTO);
        }

        keycloak.close();
        return ResponseEntity.ok(userInfoGetDTOS);
    }

    public ResponseEntity<UserInfoGetDTO> getUserByIdOrUsernameOrEmail(String id){
        UserRepresentation userRepresentation = new UserRepresentation();
        Keycloak keycloak = keycloakProvider.getInstance();
        UserInfoGetDTO userInfoGetDTO;

        userRepresentation = keycloak.realm(realm).users().search(id, true).get(0);
        if(userRepresentation == null){
            userRepresentation = keycloak.realm(realm).users().searchByUsername(id, true).get(0);
        }
        if(userRepresentation == null){
            userRepresentation = keycloak.realm(realm).users().searchByEmail(id, true).get(0);
        }

        if(userRepresentation == null){
            return ResponseEntity.ok(new UserInfoGetDTO());
        }
        userInfoGetDTO = UserInfoMapper.toUserDTO(userRepresentation);

        keycloak.close();
        return ResponseEntity.ok(userInfoGetDTO);
    }
    public ResponseEntity<List<UserSessionGetDTO>> getUserSessionById(String id){
        Keycloak keycloak = keycloakProvider.getInstance();
        List<UserSessionGetDTO> userSessionGetDTOS = new ArrayList<>();

        List<UserSessionRepresentation> userSessionRepresentationList = keycloak.realm(realm)
                .users()
                .get(id).getUserSessions();

        for(UserSessionRepresentation session: userSessionRepresentationList){
            userSessionGetDTOS.add(UserSessionMapper.userSessionDTO(session));
        }
        keycloak.close();

        return ResponseEntity.ok(userSessionGetDTOS);
    }

    public ResponseEntity<UserInfoGetDTO> createUser(UserInfoPostDTO userInfoPostDTO){
        Keycloak keycloak = keycloakProvider.getInstance();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userInfoPostDTO.getUsername());
        user.setEmail(userInfoPostDTO.getEmail());
        user.setFirstName(userInfoPostDTO.getFirstName());
        user.setLastName(userInfoPostDTO.getLastName());
        user.setEnabled(true);
        user.setAttributes(userInfoPostDTO.getAttributes());
        user.setRealmRoles(userInfoPostDTO.getRoles());
        user.setGroups(userInfoPostDTO.getGroups());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userInfoPostDTO.getPassword());
        credential.setTemporary(false);

        Response response = keycloak.realm(realm).users().create(user);
        if(response.getStatus() == 201){
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).resetPassword(credential);
        } else {
            throw new KeycloakException();
        }

        UserInfoGetDTO userInfoGetDTO = new UserInfoGetDTO();
        userInfoGetDTO.setUsername(userInfoPostDTO.getUsername());

        keycloak.close();

        return ResponseEntity.ok(userInfoGetDTO);
    }

}
