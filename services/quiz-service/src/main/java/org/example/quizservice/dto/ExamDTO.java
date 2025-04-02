package org.example.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizservice.enums.ExamType;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    private Long id;
    private Long courseId;
    private String name;
    private String description;
    private ExamType examType;
    private Long duration;
    private Float totalScore;
    private Boolean shuffleQuestions;
    private Boolean shuffleAnswers;
    private Date createdTime;
    private Date startTime;
    private Date endTime;
    private List<ExamQuestionDTO> examQuestions;
}
