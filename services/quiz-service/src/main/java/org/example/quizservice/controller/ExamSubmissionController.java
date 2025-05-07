package org.example.quizservice.controller;

import com.example.commondto.dto.ResultDTO;
import org.example.quizservice.dto.*;
import org.example.quizservice.services.ExamSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExamSubmissionController {
    
    @Autowired
    private ExamSubmissionService examSubmissionService;

    @PostMapping("/startExam/{examId}")
    public ResponseEntity<ResultDTO> startExam(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examId) {
        return examSubmissionService.startExam(validateUserId, examId);
    }
    

    @PostMapping("/submitMultipleChoiceAnswer")
    public ResponseEntity<ResultDTO> submitMultipleChoiceAnswer(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody MultipleChoiceSubmissionDTO submissionDTO) {
        return examSubmissionService.submitMultipleChoiceAnswer(validateUserId, submissionDTO);
    }


    @PostMapping("/submitEssayAnswer")
    public ResponseEntity<ResultDTO> submitEssayAnswer(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody EssaySubmissionDTO submissionDTO) {
        return examSubmissionService.submitEssayAnswer(validateUserId, submissionDTO);
    }


    @PostMapping("/submitCodingAnswer")
    public ResponseEntity<ResultDTO> submitCodingAnswer(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody CodingSubmissionDTO submissionDTO) {
        return examSubmissionService.submitCodingAnswer(validateUserId, submissionDTO);
    }


    @PostMapping("/submit/{examSubmissionId}")
    public ResponseEntity<ResultDTO> submitExam(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examSubmissionId) {
        return examSubmissionService.submitExam(validateUserId, examSubmissionId);
    }


    @GetMapping("/getExamSubmission/{submissionId}")
    public ResponseEntity<ResultDTO> getExamSubmission(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long submissionId) {
        return examSubmissionService.getExamSubmission(validateUserId, submissionId);
    }


    @GetMapping("/getExamSubmissions/{examId}")
    public ResponseEntity<ResultDTO> getExamSubmissions(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examId) {
        return examSubmissionService.getExamSubmissions(validateUserId, examId);
    }

    @GetMapping("/getExamQuestions/{examSubmissionId}")
    public ResponseEntity<ResultDTO> getExamQuestions(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examSubmissionId) {
        return examSubmissionService.getExamQuestions(validateUserId, examSubmissionId);
    }

    @GetMapping("/getQuestionSubmissions/{examSubmissionId}")
    public ResponseEntity<ResultDTO> getQuestionSubmissions(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examSubmissionId) {
        return examSubmissionService.getQuestionSubmissions(examSubmissionId, validateUserId);
    }
    @PostMapping("/executeCode")
    public ResponseEntity<ResultDTO> executeCode(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody CodeExecutionRequestDTO requestDTO) {
        return examSubmissionService.codeExecute(validateUserId, requestDTO);
    }

    @PostMapping("/gradeEssayQuestion")
    public ResponseEntity<ResultDTO> gradeEssayQuestion(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody Map<String, Object> requestBody) {
        return examSubmissionService.gradeEssayQuestion(validateUserId, requestBody);
    }

    @PostMapping("/gradeCodingQuestion")
    public ResponseEntity<ResultDTO> gradeCodingQuestion(
            @RequestHeader("X-User-Id") String validateUserId,
            @RequestBody Map<String, Object> requestBody) {
        return examSubmissionService.gradeCodingQuestion(validateUserId, requestBody);
    }

    @PostMapping("/finalizeGrading/{examSubmissionId}")
    public ResponseEntity<ResultDTO> finalizeGrading(
            @RequestHeader("X-User-Id") String validateUserId,
            @PathVariable Long examSubmissionId) {
        return examSubmissionService.finalizeGrading(validateUserId, examSubmissionId);
    }
}
