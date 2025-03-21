package com.example.courseservice.controller;

import com.example.courseservice.dto.AIChatDTO;
import com.example.courseservice.dto.AIChatSessionDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.GeminiAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class AiChatController {
    @Autowired
    private GeminiAIService geminiAIService;

    @GetMapping("/getAiChatSessionByLessonId")
    public ResponseEntity<ResultDTO> getAiChatSessionByLessonId(@RequestParam Long lessonId){
        return geminiAIService.getAiChatSessionByLessonId(lessonId);
    }

    @GetMapping("/getAllAiChatBySessionId")
    public ResponseEntity<ResultDTO> getAllAiChatBySessionId(@RequestParam Long sessionId){
        return geminiAIService.getAllAiChatBySessionId(sessionId);
    }

    @PostMapping("/startAiSession")
    public ResponseEntity<ResultDTO> startSession(@RequestBody AIChatSessionDTO aiChatSessionDTO,
                                                  @RequestHeader("X-User-Id") String userId){
        return geminiAIService.startSession(aiChatSessionDTO,userId);
    }

    @PostMapping("/sendMessageInSession")
    public ResponseEntity<ResultDTO> sendMessageInSession(@RequestBody AIChatDTO aiChatDTO,
                                                          @RequestParam Long lessonId){
        return geminiAIService.sendMessageInSession(aiChatDTO, lessonId);
    }

    @DeleteMapping("/deleteSession")
    public ResponseEntity<ResultDTO> deleteSession(@RequestParam Long sessionId){
        return geminiAIService.deleteSession(sessionId);
    }


}
