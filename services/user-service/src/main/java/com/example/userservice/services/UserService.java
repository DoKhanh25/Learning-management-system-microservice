package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NoArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    KeycloakProvider keycloakProvider;

    @Value("${keycloak.realm}")
    String realm;

    public ResponseEntity<List<UserRepresentation>> getUsers(){
        Keycloak keycloak = keycloakProvider.getInstance();
        List<UserRepresentation> userList = keycloak.realm(realm).users().list();
        return ResponseEntity.ok(userList);
    }

}
