package com.example.userservice.dto;

import lombok.Data;
import java.util.Set;

@Data
public class ScopePermissionDTO {
    String name;
    String description;
    String resource;
    Set<String> scopes;
    Set<String> policies;
    String decisionStrategy;

}
