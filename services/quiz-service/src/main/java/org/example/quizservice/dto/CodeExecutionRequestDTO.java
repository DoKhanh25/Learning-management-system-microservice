package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeExecutionRequestDTO {
    private Long questionId;
    private String version;
    private String language;
    private String stdin;
    private Long examSubmissionId;
}
