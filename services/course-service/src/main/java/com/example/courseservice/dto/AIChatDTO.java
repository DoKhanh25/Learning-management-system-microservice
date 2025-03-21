package com.example.courseservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatDTO {
    Long id;
    AIChatSessionDTO session;
    String userMessage;
    String geminiResponse;
    Integer messageOrder;
    Date createdTime;
    Long sessionId;
}
