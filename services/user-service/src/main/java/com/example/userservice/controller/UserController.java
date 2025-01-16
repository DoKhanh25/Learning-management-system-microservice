package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserInfoGetDTO>> getAllUser(){
        log.info("UserController: getUser()");
        return userService.getAllUsers();
    }

    @GetMapping("/searchUsers")
    public ResponseEntity<List<UserInfoGetDTO>> searchUser(@RequestParam String id){
        log.info("UserController: getUserById()", id);
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











}
