package com.example.userservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
@Data
public class UserInfoGetDTO {
    String userId;
    String username;
    String email;
    List<String> roles;
    List<String> groups;
    String firstName;
    String lastName;
    Boolean enable;
    Map<String, List<String>> attributes;

}
