package com.example.userservice.controller;

import com.example.userservice.PermissionUtils;
import com.example.userservice.dto.*;
import com.example.userservice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;
    @GetMapping("/getAllUsers")
    public ResponseEntity<ResultDTO> getAllUser(){
        return userService.getAllUsers();
    }

    @GetMapping("/searchUsers")
    public ResponseEntity<ResultDTO> searchUser(@RequestParam("searchQuery") String searchQuery){
        return userService.searchUserByUsernameOrEmail(searchQuery);
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserInfoGetDTO> getUser(@RequestParam String id){
        return userService.getUserByIdOrUsernameOrEmail(id);
    }

    @GetMapping("/getUserSession")
    public ResponseEntity<List<UserSessionGetDTO>> getUserSession(@RequestParam String id){
        return userService.getUserSessionById(id);
    }
//    @GetMapping("/getAllRoles")
//    public ResponseEntity<List<RolesGetDTO>> getAllRoles(){
//        return userService.getRolesKeycloak();
//    }

    @PostMapping("/getUsersByIds")
    public ResponseEntity<ResultDTO> getUsersByIds(@RequestBody List<String> userIds){
        return userService.getUsersByIds(userIds);
    }

    @PostMapping("/createUser")
    public ResponseEntity<ResultDTO> createUser(@RequestBody UserInfoPostDTO userInfoPostDTO,
                                                @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("user-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return userService.createUser(userInfoPostDTO);
    }

    @GetMapping("/getUsersExcelSample")
    public ResponseEntity<Resource> getUserSampleFile() throws IOException {
        return userService.getSampleCreateUsersExcel();
    }

    @PostMapping(value = "/uploadUsersExcel", consumes = {"multipart/form-data"})
    public ResponseEntity<Resource> uploadUsersCreateExcel(@RequestParam(name = "file") MultipartFile file,
                                                           @RequestHeader("X-Resource-Scopes") String headers) throws Exception{
        if(!PermissionUtils.canCreate("user-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return userService.uploadUsersExcel(file);
    }

    @PostMapping(value = "updateUser/{userId}")
    public ResponseEntity<ResultDTO> updateUser(@RequestBody UserInfoPostDTO userInfoPostDTO,
                                                @PathVariable String userId,
                                                @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("user-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return userService.updateUser(userInfoPostDTO, userId);
    }

    @PostMapping(value = "/disableUsers")
    public ResponseEntity<ResultDTO> disableUsers(@RequestBody List<String> userIds, @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("user-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return userService.disableUsers(userIds);
    }














}
