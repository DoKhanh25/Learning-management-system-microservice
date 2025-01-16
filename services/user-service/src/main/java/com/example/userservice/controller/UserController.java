package com.example.userservice.controller;

import com.example.userservice.dto.UserInfoGetDTO;
import com.example.userservice.dto.UserSessionGetDTO;
import com.example.userservice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/allUserList")
    public ResponseEntity<List<UserInfoGetDTO>> getUser(){
        log.info("UserController: getUser()");
        return userService.getAllUsers();
    }

    @GetMapping("/searchUser")
    public ResponseEntity<List<UserInfoGetDTO>> searchUser(@RequestParam String id){
        log.info("UserController: getUserById()", id);
        return userService.searchUserListByIdOrUsernameOrEmail(id);
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserInfoGetDTO> getUser(@RequestParam String id){
        return userService.getUserByIdOrUsernameOrEmail(id);
    }

    @GetMapping("/getUserSession")
    public ResponseEntity<List<UserSessionGetDTO>> getUserSession(@RequestParam String id){
        return userService.getUserSessionById(id);
    }









}
