package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSubmissionDTO {
    private Long id;
    private Long examSubmissionId;
    private Long questionId;
    private Float score;
    private Date submittedAt;
    private Boolean graded;
    private String feedback;
}
