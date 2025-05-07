package com.example.userservice.controller;

import com.example.userservice.PermissionUtils;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.ScopePermissionDTO;
import com.example.userservice.services.PolicyService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class PolicyController {
    @Autowired
    PolicyService policyService;

    @GetMapping("/getDependentPermission")
    public ResponseEntity<ResultDTO> getDependentPermission(@RequestParam String id,
                                                            @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.getDependentPermission(id);
    }

    @GetMapping("/getAssociatedPolicies")
    public ResponseEntity<ResultDTO> getAssociatedPolicies(@RequestParam String id,
                                                           @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.getAssociatedPolicies(id);
    }

    @GetMapping("/getClientPolicyById")
    public ResponseEntity<ResultDTO> getClientPolicyById(@RequestParam String id, @RequestHeader("X-Resource-Scopes") String headers) throws Exception{
        if(!PermissionUtils.canView("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.getClientPolicyById(id);
    }

    @GetMapping("/getClientPolicies")
    public ResponseEntity<ResultDTO> getClientPolicies(@RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.getClientPolicies();
    }

    @PostMapping("/updateClientPolicy")
    public ResponseEntity<ResultDTO> updateClientPolicy(@RequestBody PolicyRepresentation policyRepresentation,
                                                       @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.updateClientPolicy(policyRepresentation);
    }

    @PostMapping("/addClientPolicy")
    public ResponseEntity<ResultDTO> addClientPolicy(@RequestBody PolicyRepresentation policyRepresentation,
                                                    @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.addClientPolicy(policyRepresentation);
    }

    @PostMapping("/deleteClientPolicies")
    public ResponseEntity<ResultDTO> deleteClientPolicies(@RequestBody List<String> ids,
                                                         @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canDelete("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.deleteClientPolicies(ids);
    }

    @PostMapping("/createPermission")
    public ResponseEntity<ResultDTO> createPermission(@RequestBody ScopePermissionDTO scopePermissionDTO,
                                                     @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.createPermission(scopePermissionDTO);
    }

    @GetMapping("/getScopePermissionById/{id}")
    public ResponseEntity<ResultDTO> getScopePermissionById(@PathVariable String id,
                                                           @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("permission-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return policyService.getScopePermissionById(id);
    }
}
