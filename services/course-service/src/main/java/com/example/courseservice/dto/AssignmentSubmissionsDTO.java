package com.example.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSubmissionsDTO {
    Long id;
    String userId;
    AssignmentDTO assignment;
    Long assignmentId;
    Long numfiles;
    String data1;
    String data2;
    Long grade;
    String submissionComment;
    Date updatedTime;
    Date createdTime;
}
