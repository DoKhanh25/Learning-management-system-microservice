package com.example.userservice.controller;

import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.ScopePermissionDTO;
import com.example.userservice.services.PolicyService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ResultDTO> getDependentPermission(@RequestParam String id){
        return policyService.getDependentPermission(id);
    }

    @GetMapping("/getAssociatedPolicies")
    public ResponseEntity<ResultDTO> getAssociatedPolicies(@RequestParam String id){
        return policyService.getAssociatedPolicies(id);
    }

    @GetMapping("/getClientPolicyById")
    public ResponseEntity<ResultDTO> getClientPolicyById(@RequestParam String id) throws Exception{
        return policyService.getClientPolicyById(id);
    }
    @GetMapping("/getClientPolicies")
    public ResponseEntity<ResultDTO> getClientPolicies(){
        return policyService.getClientPolicies();
    }


    @PostMapping("/updateClientPolicy")
    public ResponseEntity<ResultDTO> updateClientPolicy(@RequestBody PolicyRepresentation policyRepresentation){
        return policyService.updateClientPolicy(policyRepresentation);
    }

    @PostMapping("/addClientPolicy")
    public ResponseEntity<ResultDTO> addClientPolicy(@RequestBody PolicyRepresentation policyRepresentation){
        return policyService.addClientPolicy(policyRepresentation);
    }

    @PostMapping("/deleteClientPolicies")
    public ResponseEntity<ResultDTO> deleteClientPolicies(@RequestBody List<String> ids){
        return policyService.deleteClientPolicies(ids);
    }

    @PostMapping("/createPermission")
    public ResponseEntity<ResultDTO> createPermission(@RequestBody ScopePermissionDTO scopePermissionDTO){
        return policyService.createPermission(scopePermissionDTO);
    }

    @GetMapping("/getScopePermissionById/{id}")
    public ResponseEntity<ResultDTO> getScopePermissionById(@PathVariable String id){
        return policyService.getScopePermissionById(id);
    }
}
