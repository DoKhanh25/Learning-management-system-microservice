package com.example.userservice.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class UserSessionGetDTO {
    String userId;
    String username;
    String ipAddress;
    Date start;
    Date lastAccess;
    Map<String, String> clients;
}
