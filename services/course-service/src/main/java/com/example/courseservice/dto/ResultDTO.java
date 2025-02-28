package com.example.courseservice.dto;

import lombok.Data;

@Data
public class ResultDTO {
    int status;
    String message;
    Object data;
}
