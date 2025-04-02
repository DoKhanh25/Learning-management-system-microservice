package org.example.quizservice.dto;

import lombok.Data;
import org.example.quizservice.enums.ExamType;

import java.util.Date;
import java.util.List;

@Data
public class ExamDTO {
    private Long id;
    private Long courseId;
    private String name;
    private String description;
    private ExamType examType;
    private Long duration; // in minutes
    private Float totalScore;
    private Integer numberQuestions;
    private Boolean shuffleQuestions;
    private Boolean shuffleAnswers;
    private Date createdTime;
    private Date startTime;
    private Date endTime;
    private List<ExamQuestionDTO> questions;
}
