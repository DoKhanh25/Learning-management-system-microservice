package com.example.commondto.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CohortDTO {
    Long id;
    String name;
    String description;
    List<CohortMemberDTO> cohortMembers;
    Date createdTime;
    Short available;
    Date updatedTime;
}
