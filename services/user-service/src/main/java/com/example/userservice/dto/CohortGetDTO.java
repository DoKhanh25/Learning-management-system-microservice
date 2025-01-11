package com.example.userservice.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
@Data
@SuperBuilder
public class CohortGetDTO extends CohortDTO {
    List<Long> cohortMembers;
}
