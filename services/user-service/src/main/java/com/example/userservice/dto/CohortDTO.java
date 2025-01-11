package com.example.userservice.dto;

import com.example.userservice.entity.CohortMemberEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class CohortDTO {
    Long contextId;
    String name;
    String description;
    Long idNumber;
    Date createdTime;
    Long component;
    Short available;
    Date updatedTime;
}
