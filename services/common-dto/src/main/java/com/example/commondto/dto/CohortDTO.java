package com.example.commondto.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CohortDTO {
    Long id;
    String name;
    String description;
    List<CohortMemberDTO> cohortMembers;
    Date createdTime;
    Short available;
    Date updatedTime;
}
