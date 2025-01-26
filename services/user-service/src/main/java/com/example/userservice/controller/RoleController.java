package com.example.userservice.controller;

import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.RolePostDTO;
import com.example.userservice.services.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("/getAllRoles")
    public ResponseEntity<ResultDTO> getAllRoles(){
        return roleService.getAllRealmRoles();
    }

    @GetMapping("/getCompositeRoles")
    public ResponseEntity<ResultDTO> getCompositeRolesByParentRole(@RequestParam String parentRoleName){
        return roleService.getCompositesChildrenRoleByParentRole(parentRoleName);
    }
    @GetMapping("/getRoleByName")
    public ResponseEntity<ResultDTO> getRoleByName(@RequestParam String roleName){
        return roleService.getRoleByName(roleName);
    }
    @GetMapping("/getUsersInRole")
    public ResponseEntity<ResultDTO> getUsersInRole(@RequestParam String roleName){
        return roleService.getUsersInRole(roleName);
    }

    @PostMapping("/addComposites/{roleName}")
    public ResponseEntity<ResultDTO> addComposites(@PathVariable String roleName, @RequestBody List<String> roleNames){
        return roleService.addComposites(roleNames, roleName);
    }

    @PostMapping("/unsignedComposites/{roleName}")
    public ResponseEntity<ResultDTO> unsignedComposites(@PathVariable String roleName, @RequestBody List<String> roleNames){
        return roleService.unsignedComposites(roleNames, roleName);
    }

    @PostMapping("/updateRoleDetail")
    public ResponseEntity<ResultDTO> updateRoleDetail( @RequestBody RolePostDTO rolePostDTO){
        return roleService.updateRoleDetail(rolePostDTO);
    }

    @PostMapping("/addRole")
    public ResponseEntity<ResultDTO> addRole(@RequestBody RolePostDTO rolePostDTO){
        return roleService.addRole(rolePostDTO);
    }

    @PostMapping("/addRoleComposites")
    public ResponseEntity<ResultDTO> addRoleComposites(@RequestBody List<String> childRole,
                                                       @RequestParam String parentRole){
        return roleService.addCompositesToRole(childRole, parentRole);
    }

    @PostMapping("/updateUsersInRole/{roleName}")
    public ResponseEntity<ResultDTO> updateUsersInRole(@RequestBody List<String> userIds,
                                                       @PathVariable String roleName){
        return roleService.updateUsersInRole(userIds, roleName);
    }

    @PostMapping("/removeUsersInRole/{roleName}")
    public ResponseEntity<ResultDTO> removeUsersInRole(@RequestBody List<String> userIds,
                                                       @PathVariable String roleName){
        return roleService.removeUsersInRole(userIds, roleName);
    }

    @PostMapping("/deleteRoles")
    public ResponseEntity<ResultDTO> deleteRoles(@RequestBody List<String> roleNames){
        return roleService.deleteRoles(roleNames);
    }


    @GetMapping("/getClientResources")
    public ResponseEntity<ResultDTO> getClientResources(){
        return roleService.getClientResources();
    }

    @GetMapping("/getClientScopes")
    public ResponseEntity<ResultDTO> getClientScopes(){
        return roleService.getClientScopes();
    }

    @GetMapping("/getClientPolicies")
    public ResponseEntity<ResultDTO> getClientPolicies(){
        return roleService.getClientPolicies();
    }


    @GetMapping("/getDependentPermission")
    public ResponseEntity<ResultDTO> getDependentPermission(@RequestParam String id){

        return roleService.getDependentPermission(id);
    }

    @GetMapping("/getClientPolicyById")
    public ResponseEntity<ResultDTO> getClientPolicyById(@RequestParam String id) throws Exception{
        return roleService.getClientPolicyById(id);
    }

    @PostMapping("/updateClientPolicy")
    public ResponseEntity<ResultDTO> updateClientPolicy(@RequestBody PolicyRepresentation policyRepresentation){
        return roleService.updateClientPolicy(policyRepresentation);
    }






}
