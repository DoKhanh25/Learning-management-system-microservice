package com.example.commondto.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CohortMemberDTO {
    Long id;
    CohortDTO cohort;
    String keycloakId;
    Short available;
    Date addedTime;
}
