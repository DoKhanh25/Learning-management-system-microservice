package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.RoleIdDTO;
import com.example.userservice.dto.ScopePermissionDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@NoArgsConstructor
@Slf4j
@Service
public class PolicyService {
    @Autowired
    KeycloakProvider keycloakProvider;

    @Value("${keycloak.realm}")
    String realm;

    @Value("${keycloak.authorization-client-uuid}")
    String authorizationClient;

    @Value("${keycloak.auth-server-url}")
    String keycloakServerUrl;

    public ResponseEntity<ResultDTO> getClientPolicies(){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<PolicyRepresentation> policyRepresentationList = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policies();

        resultDTO.setStatus(1);
        resultDTO.setData(policyRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getClientPolicyById(String id) throws Exception{
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        PolicyRepresentation policyRepresentation = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(id).toRepresentation();

        Map<String, String> configMap = policyRepresentation.getConfig();

        if(!configMap.isEmpty() && configMap.containsKey("roles")){
            ObjectMapper objectMapper = new ObjectMapper();
            List<RoleIdDTO> listRoleIds = objectMapper.readValue(configMap.get("roles"), new TypeReference<List<RoleIdDTO>>() {});
            List<String> ids = new ArrayList<>();
            for (RoleIdDTO roleId: listRoleIds) {
                ids.add(roleId.getId());
            }
            List<RoleRepresentation> roleRepresentationList = getRoleListByIds(ids);
            configMap.put("roleDetails", objectMapper.writeValueAsString(roleRepresentationList));
            policyRepresentation.setConfig(configMap);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(policyRepresentation);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public List<RoleRepresentation> getRoleListByIds(List<String> ids){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<RoleRepresentation> roleRepresentationList = keycloak.realm(realm).roles().list().stream().filter(
                (e) -> ids.contains(e.getId())).toList();

        resultDTO.setStatus(1);
        resultDTO.setData(roleRepresentationList);
        return roleRepresentationList;
    }

    public ResponseEntity<ResultDTO> updateClientPolicy(PolicyRepresentation policyRepresentation){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        if(policyRepresentation.getConfig() != null ){
            policyRepresentation.getConfig().remove("roleDetails");
        }

        PolicyRepresentation representation = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(policyRepresentation.getId()).toRepresentation();

        representation.setConfig(policyRepresentation.getConfig());
        representation.setDescription(policyRepresentation.getDescription());
        representation.setLogic(policyRepresentation.getLogic());
        representation.setConfig(policyRepresentation.getConfig());


        keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(policyRepresentation.getId()).update(representation);

        resultDTO.setStatus(1);
        resultDTO.setData(keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(policyRepresentation.getId()).toRepresentation());
        return ResponseEntity.ok(resultDTO);
    }


    public ResponseEntity<ResultDTO> getAssociatedPolicies(String id){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<PolicyRepresentation> policyRepresentationList = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(id).associatedPolicies();

        resultDTO.setStatus(1);
        resultDTO.setData(policyRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getDependentPermission(String id){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<PolicyRepresentation> policyRepresentationList = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().policy(id).dependentPolicies();

        resultDTO.setStatus(1);
        resultDTO.setData(policyRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> addClientPolicy(PolicyRepresentation policyRepresentation){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        Response response = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .policies().create(policyRepresentation);


        if(response.getStatus() ==  Response.Status.CREATED.getStatusCode()){
            resultDTO.setStatus(1);
            resultDTO.setData(null);
            resultDTO.setMessage("success");
        } else {
            String errorDetails = response.readEntity(String.class);
            System.out.println("Error creating policy: " + errorDetails);
            resultDTO.setStatus(response.getStatus());
            resultDTO.setData(errorDetails);
        }

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteClientPolicies(List<String> ids){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        for (String id: ids){
            keycloak.realm(realm)
                    .clients()
                    .get(authorizationClient)
                    .authorization()
                    .policies().policy(id).remove();
        }


        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> createPermission(ScopePermissionDTO scopePermissionDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        ScopePermissionRepresentation scopePermissionRepresentation = new ScopePermissionRepresentation();
        Set<String> resources = new HashSet<>();
        resources.add(scopePermissionDTO.getResource());

        scopePermissionRepresentation.setName(scopePermissionDTO.getName());
        scopePermissionRepresentation.setDescription(scopePermissionDTO.getDescription());
        scopePermissionRepresentation.setResources(resources);
        scopePermissionRepresentation.setDecisionStrategy(DecisionStrategy.valueOf(scopePermissionDTO.getDecisionStrategy().toUpperCase()));
        scopePermissionRepresentation.setPolicies(scopePermissionDTO.getPolicies());
        scopePermissionRepresentation.setScopes(scopePermissionDTO.getScopes());

        Response response = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .permissions().scope().create(scopePermissionRepresentation);

        if(response.getStatus() ==  Response.Status.CREATED.getStatusCode()){
            resultDTO.setStatus(1);
            resultDTO.setData(null);
            resultDTO.setMessage("success");
        } else {
            String errorDetails = response.readEntity(String.class);
            System.out.println("Error creating policy: " + errorDetails);
            resultDTO.setStatus(response.getStatus());
            resultDTO.setData(errorDetails);
        }

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getScopePermissionById(String id){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        Set<String> scopes = new HashSet<>();
        Set<String> resources = new HashSet<>();
        Set<String> policies = new HashSet<>();


        ScopePermissionRepresentation scopePermissionRepresentation = keycloak.realm(realm).clients()
                .get(authorizationClient)
                .authorization()
                .permissions().scope().findById(id).toRepresentation();

        List<ScopeRepresentation> scopeRepresentationList = keycloak.realm(realm).clients()
                .get(authorizationClient)
                .authorization().policies().policy(id).scopes();

        List<ResourceRepresentation> resourceRepresentationList = keycloak.realm(realm).clients()
                .get(authorizationClient)
                .authorization().policies().policy(id).resources();

        List<PolicyRepresentation> policyRepresentationList = keycloak.realm(realm).clients()
                .get(authorizationClient)
                .authorization().policies().policy(id).associatedPolicies();


        for(ScopeRepresentation s: scopeRepresentationList){
            scopes.add(s.getName());
        }
        for (ResourceRepresentation s : resourceRepresentationList){
            resources.add(s.getName());
        }
        for (PolicyRepresentation s: policyRepresentationList){
            policies.add(s.getName());
        }

        scopePermissionRepresentation.setScopes(scopes);
        scopePermissionRepresentation.setResources(resources);
        scopePermissionRepresentation.setPolicies(policies);

        resultDTO.setStatus(1);
        resultDTO.setData(scopePermissionRepresentation);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateScopePermission(ScopePermissionDTO scopePermissionDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        Set<String> resources = new HashSet<>();
        resources.add(scopePermissionDTO.getResource());

        ScopePermissionRepresentation scopePermissionRepresentation =
                keycloak.realm(realm).clients()
                .get(authorizationClient)
                .authorization()
                .permissions().scope().findByName(scopePermissionDTO.getName());

        scopePermissionRepresentation.setName(scopePermissionDTO.getName());
        scopePermissionRepresentation.setDescription(scopePermissionDTO.getDescription());
        scopePermissionRepresentation.setResources(resources);
        scopePermissionRepresentation.setDecisionStrategy(DecisionStrategy.valueOf(scopePermissionDTO.getDecisionStrategy().toUpperCase()));
        scopePermissionRepresentation.setPolicies(scopePermissionDTO.getPolicies());
        scopePermissionRepresentation.setScopes(scopePermissionDTO.getScopes());
        return null;
    }





}
