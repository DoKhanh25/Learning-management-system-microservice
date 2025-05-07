package org.example.quizservice.controller;

import com.example.commondto.dto.ResultDTO;
import org.example.quizservice.dto.ExamDTO;
import org.example.quizservice.dto.CodeExecutionRequestDTO;
import org.example.quizservice.services.ExamService;
import org.example.quizservice.services.ExamSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamSubmissionService examSubmissionService;

    @GetMapping("/countAllExams")
    public ResponseEntity<ResultDTO> countAllExams(){
        return examService.countAllExams();
    }

    @GetMapping("/getExamsTree")
    public ResponseEntity<ResultDTO> getExamsTree(@RequestParam Long courseId){
        return examService.getExamsTree(courseId);
    }

    @GetMapping("/getAllExamsByCourseId/{courseId}")
    public ResponseEntity<ResultDTO> getAllExamsByCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.getAllExamsByCourseId(courseId, validateUserId);
    }

    @GetMapping("/getAvailableExamsByCourseId/{courseId}")
    public ResponseEntity<ResultDTO> getAvailableExamsByCourseId(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.getAvailableExamsByCourseId(courseId, validateUserId);
    }

    @GetMapping("/getExamById/{examId}")
    public ResponseEntity<ResultDTO> getExamById(
            @PathVariable Long examId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.getExamById(examId, validateUserId);
    }

    @PostMapping("/addExam")
    public ResponseEntity<ResultDTO> createExam(
            @RequestBody ExamDTO examDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.createExam(examDTO, validateUserId);
    }

    @PutMapping("/updateExam/{examId}")
    public ResponseEntity<ResultDTO> updateExam(
            @PathVariable Long examId,
            @RequestBody ExamDTO examDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.updateExam(examId, examDTO, validateUserId);
    }

    @DeleteMapping("/deleteExam/{examId}")
    public ResponseEntity<ResultDTO> deleteExam(
            @PathVariable Long examId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.deleteExam(examId, validateUserId);
    }

    @PostMapping("/addQuestionsFromQuestionBank/{examId}")
    public ResponseEntity<ResultDTO> addQuestionsFromQuestionBank(
            @PathVariable Long examId,
            @RequestParam Long questionBankId,
            @RequestParam Integer numberOfQuestions,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.addQuestionsFromQuestionBank(examId, questionBankId, numberOfQuestions, validateUserId);
    }

    @PostMapping("/addQuestionsFromMultipleQuestionBanks/{examId}")
    public ResponseEntity<ResultDTO> addQuestionsFromMultipleQuestionBanks(
            @PathVariable Long examId,
            @RequestBody List<Long> questionBankIds,
            @RequestParam Integer numberOfQuestions,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.addQuestionsFromMultipleQuestionBanks(examId, questionBankIds, numberOfQuestions, validateUserId);
    }

    @DeleteMapping("/removeQuestionFromExam/{examId}/{questionId}")
    public ResponseEntity<ResultDTO> removeQuestionFromExam(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return examService.removeQuestionFromExam(examId, questionId, validateUserId);
    }
}
