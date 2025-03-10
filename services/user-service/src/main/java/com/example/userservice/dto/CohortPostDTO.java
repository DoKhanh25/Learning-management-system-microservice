package com.example.userservice.dto;

import lombok.Data;

import java.util.List;
@Data
public class CohortPostDTO {
    Long id;
    String name;
    String description;
    Short available;
    List<String> userIds;
}
