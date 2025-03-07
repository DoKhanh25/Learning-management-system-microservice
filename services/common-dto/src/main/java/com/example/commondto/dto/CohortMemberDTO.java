package com.example.commondto.dto;

import com.example.commondto.dto.CohortDTO;
import lombok.Data;

import java.util.Date;

@Data
public class CohortMemberDTO {
    Long id;
    CohortDTO cohort;
    String keycloakId;
    Short available;
    Date addedTime;
}
