package com.example.userservice.dto;

import lombok.Data;

@Data
public class RolesGetDTO {
    String id;
    CompositesDTO composites;
    String description;
    String name;
    Boolean isComposite;
}
