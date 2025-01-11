package com.example.userservice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CohortMemberDTO {
    String keycloakId;
    Short available;
    Date addedTime;
}
