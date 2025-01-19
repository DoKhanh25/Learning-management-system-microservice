package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.configuration.Utils;
import com.example.userservice.dto.*;
import com.example.userservice.exception.KeycloakException;
import com.example.userservice.mapper.RolesMapper;
import com.example.userservice.mapper.UserInfoMapper;
import com.example.userservice.mapper.UserSessionMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

        return ResponseEntity.ok(userInfoGetDTOS);
    }

    public ResponseEntity<List<UserInfoGetDTO>> searchUserListByUsernameOrEmail(String id){
        List<UserRepresentation> userRepresentationList;
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        Keycloak keycloak = keycloakProvider.getInstance();

        userRepresentationList = keycloak.realm(realm).users().search(id, false);

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            return ResponseEntity.ok(userInfoGetDTOS);
        }

        for(UserRepresentation userKeycloak: userRepresentationList){
            UserInfoGetDTO userInfoGetDTO = UserInfoMapper.toUserDTO(userKeycloak);
            userInfoGetDTOS.add(userInfoGetDTO);
        }

        return ResponseEntity.ok(userInfoGetDTOS);
    }

    public ResponseEntity<UserInfoGetDTO> getUserByIdOrUsernameOrEmail(String id){
        UserRepresentation userRepresentation;
        Keycloak keycloak = keycloakProvider.getInstance();
        UserInfoGetDTO userInfoGetDTO;
        List<UserRepresentation> userRepresentationList;

        try{
            userRepresentation = keycloak.realm(realm).users().get(id).toRepresentation();

            if(userRepresentation == null){
                return ResponseEntity.ok(new UserInfoGetDTO());
            }
            userInfoGetDTO = UserInfoMapper.toUserDTO(userRepresentation);

            return ResponseEntity.ok(userInfoGetDTO);

        } catch (NotFoundException exception){
            userRepresentationList = keycloak.realm(realm).users().search(id, true);

            if(userRepresentationList.isEmpty()){

                return ResponseEntity.ok(null);

            } else {
                userRepresentation = userRepresentationList.get(0);
                if(userRepresentation == null){
                    return ResponseEntity.ok(null);
                }
                userInfoGetDTO = UserInfoMapper.toUserDTO(userRepresentation);

            }

            return ResponseEntity.ok(userInfoGetDTO);
        }
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

        return ResponseEntity.ok(userSessionGetDTOS);
    }

    public ResponseEntity<ResultDTO> createUser(UserInfoPostDTO userInfoPostDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

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
        log.info("Response |  Status: {} | Status Info: {}", response.getStatus(), response.getStatusInfo());


        if(response.getStatus() ==  Response.Status.CREATED.getStatusCode()){
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).resetPassword(credential);


            resultDTO.setStatus(response.getStatus());
            resultDTO.setMessage("Success");
            resultDTO.setData(userId);

        } else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()){
            resultDTO.setStatus(response.getStatus());
            resultDTO.setMessage("Tai khoan da ton tai");
            resultDTO.setData(null);

        }

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<List<RolesGetDTO>> getRolesKeycloak(){
        Keycloak keycloak = keycloakProvider.getInstance();
        List<RoleRepresentation> rolesResources = keycloak.realm(realm).roles().list();
        log.info(rolesResources.get(0).toString());

        List<RolesGetDTO> rolesGetDTOs = new ArrayList<>();
        for (RoleRepresentation role : rolesResources){
            rolesGetDTOs.add(RolesMapper.toRolesGetDTO(role));
        }
        return  ResponseEntity.ok(rolesGetDTOs);
    }

    public ResponseEntity<Resource> getSampleCreateUsersExcel() throws IOException{

        File file = new ClassPathResource("sample/users_create_sample.xlsx").getFile();;

        try (FileInputStream fs = new FileInputStream(file);
        ) {
            InputStreamResource resource = new InputStreamResource(fs);
            HttpHeaders header = new HttpHeaders();

            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_create_sample.xlsx");
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(org.springframework.http.MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM))
                    .body(resource);

        }
    }





}
