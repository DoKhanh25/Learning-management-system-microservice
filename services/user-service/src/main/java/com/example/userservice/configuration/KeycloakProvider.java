package com.example.userservice.configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@NoArgsConstructor
@Component
public class KeycloakProvider {
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private static Keycloak keycloak = null;


    public Keycloak getInstance(){
        if(keycloak == null){
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .clientId(clientId)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .realm(realm)
                    .clientSecret(clientSecret)
                    .build();
        }
        return keycloak;
    }




}
