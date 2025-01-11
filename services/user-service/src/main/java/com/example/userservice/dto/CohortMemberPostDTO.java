package com.example.userservice.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class CohortMemberPostDTO extends CohortMemberDTO{
    Long cohort;
}
