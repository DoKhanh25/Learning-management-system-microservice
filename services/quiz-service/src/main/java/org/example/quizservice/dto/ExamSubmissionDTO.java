package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionDTO {
    private Long id;
    private String userId;
    private Long examId;
    private Date startTime;
    private Date submissionTime;
    private Float totalScore;
    private Boolean isGraded;
    private Long gradedBy;
    private Date gradingTime;
    private String feedback;
    private List<QuestionSubmissionDTO> questionSubmissions;
}
