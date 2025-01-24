package com.example.userservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class CompositesDTO {
    Set<String> realm;
    Map<String, List<String>> client;


}
