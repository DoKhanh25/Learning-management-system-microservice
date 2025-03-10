package com.example.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEnrolmentsDTO {
    private Long id;
    private Integer status;
    private EnrolDTO enrol;
    private String userId;
    private Date timeStart;
    private Date timeEnd;
    Date createdTime;
    Date updatedTime;
}
