package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
    public ResponseEntity<List<UserInfoGetDTO>> searchUser(@RequestParam String id){
        return userService.searchUserListByUsernameOrEmail(id);
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserInfoGetDTO> getUser(@RequestParam String id){
        return userService.getUserByIdOrUsernameOrEmail(id);
    }

    @GetMapping("/getUserSession")
    public ResponseEntity<List<UserSessionGetDTO>> getUserSession(@RequestParam String id){
        return userService.getUserSessionById(id);
    }
    @GetMapping("/getAllRoles")
    public ResponseEntity<List<RolesGetDTO>> getAllRoles(){
        return userService.getRolesKeycloak();
    }

    @PostMapping("/createUser")
    public ResponseEntity<ResultDTO> createUser(@RequestBody UserInfoPostDTO userInfoPostDTO){
        return userService.createUser(userInfoPostDTO);
    }

    @GetMapping("/getUsersExcelSample")
    public ResponseEntity<Resource> getUserSampleFile() throws IOException {
        return userService.getSampleCreateUsersExcel();
    }

    @PostMapping(value = "/uploadUsersExcel", consumes = {"multipart/form-data"})
    public ResponseEntity<Resource> uploadUsersCreateExcel(@RequestParam(name = "file") MultipartFile file) throws Exception{
        return userService.uploadUsersExcel(file);
    }














}
