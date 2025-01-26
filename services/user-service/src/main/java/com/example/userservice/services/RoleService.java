package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.RoleIdDTO;
import com.example.userservice.dto.RolePostDTO;
import com.example.userservice.dto.RolesGetDTO;
import com.example.userservice.mapper.RolesMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.PermissionsResource;
import org.keycloak.admin.client.resource.ScopePermissionsResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.*;
import org.keycloak.representations.idm.authorization.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.*;

@NoArgsConstructor
@Slf4j
@Service
public class RoleService {
    @Autowired
    KeycloakProvider keycloakProvider;

    @Value("${keycloak.realm}")
    String realm;

    @Value("${keycloak.authorization-client-uuid}")
    String authorizationClient;

    @Value("${keycloak.auth-server-url}")
    String keycloakServerUrl;

    public ResponseEntity<ResultDTO> getAllRealmRoles(){
        Keycloak keycloak = keycloakProvider.getInstance();
        List<RoleRepresentation> roleRepresentations = keycloak.realm(realm).roles().list();
        List<RolesGetDTO> rolesGetDTOList = new ArrayList<>();
        ResultDTO resultDTO = new ResultDTO();

        for(RoleRepresentation roleRepresentation: roleRepresentations){
            rolesGetDTOList.add(RolesMapper.toRolesGetDTO(roleRepresentation));
        }
        resultDTO.setStatus(1);
        resultDTO.setData(rolesGetDTOList);
        resultDTO.setMessage("success");

        return ResponseEntity.ok(resultDTO);
    }
    public ResponseEntity<ResultDTO> getCompositesChildrenRoleByParentRole(String parentRole){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        Set<RoleRepresentation> compositeRoles = keycloak.realm(realm)
                .roles()
                .get(parentRole).getRoleComposites();
        Set<RolesGetDTO> rolesGetDTOSet = new HashSet<>();

        for(RoleRepresentation roleRepresentation: compositeRoles){
            rolesGetDTOSet.add(RolesMapper.toRolesGetDTO(roleRepresentation));
        }
        resultDTO.setStatus(1);
        resultDTO.setData(rolesGetDTOSet);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getRoleByName(String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        RoleRepresentation roleRepresentation = keycloak.realm(realm)
                .roles()
                .get(roleName).toRepresentation();

        resultDTO.setStatus(1);
        resultDTO.setData(roleRepresentation);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getUsersInRole(String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<UserRepresentation> userRepresentationList = keycloak.realm(realm)
                .roles()
                .get(roleName).getUserMembers();

        resultDTO.setStatus(1);
        resultDTO.setData(userRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }
    public ResponseEntity<ResultDTO> addRole(RolePostDTO rolePostDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        RoleRepresentation roleRepresentation = new RoleRepresentation();

        roleRepresentation.setName(rolePostDTO.getName());
        roleRepresentation.setDescription(rolePostDTO.getDescription());

        keycloak.realm(realm).roles().create(roleRepresentation);
        RolesGetDTO rolesGetDTO = RolesMapper.toRolesGetDTO(keycloak.realm(realm).roles().get(rolePostDTO.getName()).toRepresentation());

        resultDTO.setStatus(1);
        resultDTO.setData(rolesGetDTO);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteRoles(List<String> roleNames){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

        for (String role: roleNames) {
            keycloak.realm(realm).roles().deleteRole(role);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }



    public ResponseEntity<ResultDTO> addComposites(List<String> roleAssignNames, String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<RoleRepresentation> roleRepresentationList = new ArrayList<>();
        for (String roleAssignName: roleAssignNames){
            roleRepresentationList.add(keycloak.realm(realm).roles().get(roleAssignName).toRepresentation());
        }


        keycloak.realm(realm).roles().get(roleName).addComposites(roleRepresentationList);

        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> unsignedComposites(List<String> roleUnassginNames, String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<RoleRepresentation> roleRepresentationList = new ArrayList<>();
        for (String roleUnassignName: roleUnassginNames){
            roleRepresentationList.add(keycloak.realm(realm).roles().get(roleUnassignName).toRepresentation());
        }

        keycloak.realm(realm).roles().get(roleName).deleteComposites(roleRepresentationList);

        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }





    public ResponseEntity<ResultDTO> addCompositesToRole(List<String> roleChildNames, String parentRole){
        List<RoleRepresentation> roleRepresentationChildList = new ArrayList<>();
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();


        for(String roleChild: roleChildNames){
            RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(roleChild).toRepresentation();
            roleRepresentationChildList.add(roleRepresentation);
        }

        keycloak.realm(realm)
                .roles()
                .get(parentRole)
                .addComposites(roleRepresentationChildList);

        RolesGetDTO rolesGetDTO = RolesMapper.toRolesGetDTO(keycloak.realm(realm).roles().get(parentRole).toRepresentation());

        resultDTO.setStatus(1);
        resultDTO.setData(rolesGetDTO);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateRoleDetail(RolePostDTO rolePostDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(rolePostDTO.getName()).toRepresentation();
        ResultDTO resultDTO = new ResultDTO();

        roleRepresentation.setDescription(rolePostDTO.getDescription());
        keycloak.realm(realm).roles().get(roleRepresentation.getName()).update(roleRepresentation);
        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateUsersInRole(List<String> userIds, String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

        for (String user: userIds){
            RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            List<RoleRepresentation> realmRoles = new ArrayList<>();
            realmRoles.add(roleRepresentation);
            keycloak.realm(realm).users().get(user).roles().realmLevel().add(realmRoles);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> removeUsersInRole(List<String> userIds, String roleName){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

        for (String user: userIds){
            RoleRepresentation roleRepresentation = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            List<RoleRepresentation> realmRoles = new ArrayList<>();
            realmRoles.add(roleRepresentation);
            keycloak.realm(realm).users().get(user).roles().realmLevel().remove(realmRoles);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(null);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }





    public ResponseEntity<ResultDTO> getClientResources(){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<ResourceRepresentation> resourceRepresentationList = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .resources()
                .resources();


        resultDTO.setStatus(1);
        resultDTO.setData(resourceRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getClientScopes(){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<ScopeRepresentation> scopeRepresentationList = keycloak.realm(realm)
                .clients()
                .get(authorizationClient)
                .authorization()
                .scopes().scopes();

        resultDTO.setStatus(1);
        resultDTO.setData(scopeRepresentationList);
        resultDTO.setMessage("success");
        return ResponseEntity.ok(resultDTO);
    }

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


}
