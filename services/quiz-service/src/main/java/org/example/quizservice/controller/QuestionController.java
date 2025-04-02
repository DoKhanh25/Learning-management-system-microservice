package org.example.quizservice.controller;

import com.example.commondto.dto.ResultDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.quizservice.dto.CodingQuestionDTO;
import org.example.quizservice.dto.EssayQuestionDTO;
import org.example.quizservice.dto.MultipleChoiceQuestionDTO;
import org.example.quizservice.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<ResultDTO> getQuestionsByQuestionBankId(
            @RequestParam Long questionBankId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.findQuestionEntitiesByQuestionBankId(questionBankId, validateUserId);
    }

    @PostMapping("/addMultipleChoiceQuestion")
    public ResponseEntity<ResultDTO> addMultipleChoiceQuestion(
            @RequestBody MultipleChoiceQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.addMultipleChoiceQuestion(questionDTO, validateUserId);
    }

    @PostMapping("/addEssayQuestion")
    public ResponseEntity<ResultDTO> addEssayQuestion(
            @RequestBody EssayQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.addEssayQuestion(questionDTO, validateUserId);
    }

    @PostMapping("/addCodingQuestion")
    public ResponseEntity<ResultDTO> addCodingQuestion(
            @RequestBody CodingQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.addCodingQuestion(questionDTO, validateUserId);
    }

    @PostMapping("/importEssayQuestionsFromExcel")
    public ResponseEntity<ResultDTO> importEssayQuestionsFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long questionBankId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.addMultipleEssayQuestionsFromExcel(file, validateUserId, questionBankId);
    }

    @PostMapping("/importMultipleChoiceQuestionsFromExcel")
    public ResponseEntity<ResultDTO> importMultipleChoiceQuestionsFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long questionBankId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.addMultipleChoiceQuestionsFromExcel(file, validateUserId, questionBankId);
    }

    @GetMapping("/downloadEssaySample")
    public ResponseEntity<?> downloadEssaySample() throws IOException {
        Resource resource = new ClassPathResource("sample/essay_sample.xlsx");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=essay_sample.xlsx");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/downloadMultipleChoiceSample")
    public ResponseEntity<?> downloadMultipleChoiceSample() throws IOException {
        Resource resource = new ClassPathResource("sample/multiple_choice_sample.xlsx");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=multiple_choice_sample.xlsx");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PutMapping("/updateMultipleChoiceQuestion/{id}")
    public ResponseEntity<ResultDTO> updateMultipleChoiceQuestion(
            @PathVariable Long id,
            @RequestBody MultipleChoiceQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.updateMultipleChoiceQuestion(id, questionDTO, validateUserId);
    }

    @PutMapping("/updateEssayQuestion/{id}")
    public ResponseEntity<ResultDTO> updateEssayQuestion(
            @PathVariable Long id,
            @RequestBody EssayQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.updateEssayQuestion(id, questionDTO, validateUserId);
    }

    @PutMapping("/updateCodingQuestion/{id}")
    public ResponseEntity<ResultDTO> updateCodingQuestion(
            @PathVariable Long id,
            @RequestBody CodingQuestionDTO questionDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.updateCodingQuestion(id, questionDTO, validateUserId);
    }

    @DeleteMapping("/deleteQuestion/{id}")
    public ResponseEntity<ResultDTO> deleteQuestion(@PathVariable Long id, @RequestHeader("X-User-Id") String validateUserId) {
        return questionService.deleteQuestion(id, validateUserId);
    }
}
