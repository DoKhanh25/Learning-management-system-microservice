package org.example.quizservice.controller;

import com.example.commondto.dto.ResultDTO;
import org.example.quizservice.dto.QuestionBankDTO;
import org.example.quizservice.services.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @GetMapping("/getQuestionBankByQuestionBankId")
    public ResponseEntity<ResultDTO> getQuestionBankByQuestionBankId(@RequestParam("id") Long questionBankId,
                                                                     @RequestHeader("X-User-Id") String validateUserId) {
        return questionBankService.getQuestionBankByQuestionBankId(questionBankId, validateUserId);
    }

    @GetMapping("/getAllQuestionBanksByCourseId")
    public ResponseEntity<ResultDTO> getAllQuestionBanksByCourseId(
            @RequestParam Long courseId,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionBankService.getAllQuestionBanksByCourseId(courseId, validateUserId);
    }

    @PostMapping("/createQuestionBank")
    public ResponseEntity<ResultDTO> createQuestionBank(
            @RequestBody QuestionBankDTO questionBankDTO,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionBankService.saveQuestionBank(questionBankDTO, validateUserId);
    }

    @DeleteMapping("/deleteQuestionBank/{id}")
    public ResponseEntity<ResultDTO> deleteQuestionBank(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String validateUserId) {
        return questionBankService.deleteQuestionBank(id, validateUserId);
    }
}
