package com.example.userservice.dto;

import lombok.Data;

@Data
public class RolesGetDTO {
    String id;
    String composites;
    String description;
    String name;
    Boolean isComposite;
}
