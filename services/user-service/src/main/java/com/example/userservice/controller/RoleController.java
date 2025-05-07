package com.example.userservice.controller;

import com.example.userservice.PermissionUtils;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.dto.RolePostDTO;
import com.example.userservice.services.RoleService;
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
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("/getAllRoles")
    public ResponseEntity<ResultDTO> getAllRoles(@RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return roleService.getAllRealmRoles();
    }

    @GetMapping("/getCompositeRoles")
    public ResponseEntity<ResultDTO> getCompositeRolesByParentRole(@RequestParam String parentRoleName,
                                                                  @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getCompositesChildrenRoleByParentRole(parentRoleName);
    }

    @GetMapping("/getRoleByName")
    public ResponseEntity<ResultDTO> getRoleByName(@RequestParam String roleName,
                                                  @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getRoleByName(roleName);
    }
    @GetMapping("/getUsersInRole")
    public ResponseEntity<ResultDTO> getUsersInRole(@RequestParam String roleName,
                                                   @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getUsersInRole(roleName);
    }

    @PostMapping("/addComposites/{roleName}")
    public ResponseEntity<ResultDTO> addComposites(@PathVariable String roleName,
                                                  @RequestBody List<String> roleNames,
                                                  @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.addComposites(roleNames, roleName);
    }

    @PostMapping("/unsignedComposites/{roleName}")
    public ResponseEntity<ResultDTO> unsignedComposites(@PathVariable String roleName,
                                                       @RequestBody List<String> roleNames,
                                                       @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.unsignedComposites(roleNames, roleName);
    }

    @PostMapping("/updateRoleDetail")
    public ResponseEntity<ResultDTO> updateRoleDetail(@RequestBody RolePostDTO rolePostDTO,
                                                     @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.updateRoleDetail(rolePostDTO);
    }

    @PostMapping("/addRole")
    public ResponseEntity<ResultDTO> addRole(@RequestBody RolePostDTO rolePostDTO,
                                           @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.addRole(rolePostDTO);
    }

    @PostMapping("/addRoleComposites")
    public ResponseEntity<ResultDTO> addRoleComposites(@RequestBody List<String> childRole,
                                                      @RequestParam String parentRole,
                                                      @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.addCompositesToRole(childRole, parentRole);
    }

    @PostMapping("/updateUsersInRole/{roleName}")
    public ResponseEntity<ResultDTO> updateUsersInRole(@RequestBody List<String> userIds,
                                                      @PathVariable String roleName,
                                                      @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.updateUsersInRole(userIds, roleName);
    }

    @PostMapping("/removeUsersInRole/{roleName}")
    public ResponseEntity<ResultDTO> removeUsersInRole(@RequestBody List<String> userIds,
                                                      @PathVariable String roleName,
                                                      @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canDelete("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.removeUsersInRole(userIds, roleName);
    }

    @PostMapping("/deleteRoles")
    public ResponseEntity<ResultDTO> deleteRoles(@RequestBody List<String> roleNames,
                                               @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canDelete("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.deleteRoles(roleNames);
    }


    @GetMapping("/getClientResources")
    public ResponseEntity<ResultDTO> getClientResources(@RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getClientResources();
    }

    @GetMapping("/getClientScopes")
    public ResponseEntity<ResultDTO> getClientScopes(@RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getClientScopes();
    }

    @GetMapping("/getScopesByResource")
    public ResponseEntity<ResultDTO> getScopesByResource(@RequestParam String id,
                                                       @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("role-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return roleService.getScopesByResource(id);
    }

}
