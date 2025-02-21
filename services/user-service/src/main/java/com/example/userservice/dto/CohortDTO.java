package com.example.userservice.dto;

import com.example.userservice.entity.CohortMemberEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class CohortDTO {
    Long id;
    String name;
    String description;
    Short available;
    List<String> userIds;
}
