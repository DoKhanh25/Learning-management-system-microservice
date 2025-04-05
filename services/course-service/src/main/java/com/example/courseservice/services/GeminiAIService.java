package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AIChatDTO;
import com.example.courseservice.dto.AIChatSessionDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.AIChatEntity;
import com.example.courseservice.entity.AIChatSessionEntity;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.enums.QType;
import com.example.courseservice.feign.GeminiClient;
import com.example.courseservice.mapper.AIChatMapper;
import com.example.courseservice.mapper.AIChatSessionMapper;
import com.example.courseservice.repository.AIChatRepository;
import com.example.courseservice.repository.AIChatSessionRepository;
import com.example.courseservice.repository.LessonPagesRepository;
import com.example.courseservice.repository.LessonRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    String apiKey;

    private final GeminiClient geminiClient;

    @Autowired
    AIChatRepository aiChatRepository;

    @Autowired
    LessonPagesRepository lessonPagesRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    AIChatSessionRepository aiChatSessionRepository;

    @Autowired
    AIChatSessionMapper aiChatSessionMapper;

    @Autowired
    AIChatMapper aiChatMapper;

    private final ObjectMapper objectMapper;

    public GeminiAIService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }


    public ResponseEntity<ResultDTO> getAiChatSessionByLessonId(Long lessonId){
        ResultDTO resultDTO = new ResultDTO();
        if(lessonId == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AIChatSessionEntity aiChatSessionEntity = aiChatSessionRepository.findAIChatSessionByLessonId(lessonId);
        resultDTO.setStatus(1);
        resultDTO.setMessage("OK");
        resultDTO.setData(aiChatSessionMapper.toDto(aiChatSessionEntity, new CycleAvoidingMappingContext()));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> getAllAiChatBySessionId(Long sessionId){
        ResultDTO resultDTO = new ResultDTO();
        if(sessionId == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        List<AIChatEntity> aiChatEntities = aiChatSessionRepository.findAIChatEntitiesBySessionId(sessionId);
        resultDTO.setStatus(1);
        resultDTO.setMessage("OK");
        resultDTO.setData(aiChatMapper.toDtoList(aiChatEntities, new CycleAvoidingMappingContext()));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> startSession(AIChatSessionDTO aiChatSessionDTO, String userId) {
        ResultDTO resultDTO = new ResultDTO();
        if(aiChatSessionDTO == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (aiChatSessionDTO.getSessionName() == null || aiChatSessionDTO.getSessionName().isEmpty()) {
            resultDTO.setMessage("Session name is required");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AIChatSessionEntity aiChatSessionEntity = aiChatSessionMapper.toEntity(aiChatSessionDTO, new CycleAvoidingMappingContext());
        aiChatSessionEntity.setCreatedTime(new Date());
        aiChatSessionEntity.setUserId(userId);

        LessonEntity lessonEntity = lessonRepository.findById(aiChatSessionDTO.getLessonId()).orElse(null);
        if(lessonEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        aiChatSessionEntity.setLesson(lessonEntity);
        aiChatSessionEntity = aiChatSessionRepository.save(aiChatSessionEntity);
        resultDTO.setMessage("Session created");
        resultDTO.setStatus(1);
        resultDTO.setData(aiChatSessionMapper.toDto(aiChatSessionEntity, new CycleAvoidingMappingContext()));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);

    }

    public ResponseEntity<ResultDTO> sendMessageInSession(AIChatDTO aiChatDTO, Long lessonId) {
        Optional<AIChatSessionEntity> sessionOptional = aiChatSessionRepository.findById(aiChatDTO.getSessionId());
        Optional<LessonEntity> lessonEntityOptional = lessonRepository.findById(lessonId);

        ResultDTO resultDTO = new ResultDTO();
        StringBuilder context = new StringBuilder();

        if(sessionOptional.isEmpty()){
            resultDTO.setMessage("Session not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(lessonEntityOptional.isEmpty()){
            resultDTO.setMessage("Lesson not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        AIChatSessionEntity session = sessionOptional.get();

        // If this is the first message, build context from lesson pages
        if(session.getMessages() == null || session.getMessages().isEmpty()){
            List<LessonPagesEntity> lessonPagesEntities = lessonRepository.findLessonPagesEntitiesByLessonId(lessonId);
            if(lessonPagesEntities.isEmpty()){
                resultDTO.setMessage("Lesson pages not found");
                return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
            }

            // Build initial context from lesson pages
            context.append("Giao tiếp bằng tiếng việt\n");
            context.append("Context for this conversation:\n");
            for (LessonPagesEntity page : lessonPagesEntities) {
                if(page.getQType().equals(QType.CONTENT)){
                    String pageContent = page.getContent();
                    context.append(pageContent).append("\n");
                } else if (page.getQType().equals(QType.DOCUMENT)){
                    String pageContent = this.extractDocumentContent(page);
                    context.append(pageContent).append("\n");
                }
            }
            context.append("\n");

            // Update session with lesson info

            session.setLesson(lessonEntityOptional.get());
            session.setContextUsed(context.toString());
            session = aiChatSessionRepository.save(session);
        } else {
            // Use existing context
            if (session.getContextUsed() != null && !session.getContextUsed().isEmpty()) {
                context.append(session.getContextUsed());
            }
        }

        // Add previous messages
        List<AIChatEntity> previousMessages = aiChatRepository.findAiChatEntitiesBySessionIdOrderByCreatedTimeAsc(aiChatDTO.getSessionId());
        for (AIChatEntity msg : previousMessages) {
            context.append("user: ").append(msg.getUserMessage()).append("\n");
            context.append("model: ").append(msg.getGeminiResponse()).append("\n");
        }
        context.append("user: ").append(aiChatDTO.getUserMessage()).append("\n");

        try {
            // Send request to Gemini
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + context.toString().replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
            String response = geminiClient.generateContent(requestBody);
            JsonNode root = objectMapper.readTree(response);
            String geminiResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // Save new message
            AIChatEntity message = new AIChatEntity();
            message.setSession(session);
            message.setUserMessage(aiChatDTO.getUserMessage());
            message.setGeminiResponse(geminiResponse);
            message.setCreatedTime(new Date());
            aiChatRepository.save(message);

            // Update session
            session.setUpdatedTime(new Date());
            aiChatSessionRepository.save(session);

            // Return response
            resultDTO.setStatus(1);
            resultDTO.setMessage("Message sent successfully");
            resultDTO.setData(geminiResponse);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Error processing Gemini response: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResultDTO> deleteSession(Long sessionId){
        ResultDTO resultDTO = new ResultDTO();

        if(sessionId == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<AIChatSessionEntity> sessionOptional = aiChatSessionRepository.findById(sessionId);
        if(sessionOptional.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        aiChatSessionRepository.deleteById(sessionId);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Session deleted successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }


    private String extractDocumentContent(LessonPagesEntity lessonPage) {
        try {
            if (lessonPage.getContent() == null || lessonPage.getContent().isEmpty()) {
                return "";
            }

            String documentPath = lessonPage.getContent().trim();
            File file = new File(documentPath);

            if (!file.exists() || !file.canRead()) {
                return "Document not found or cannot be read: " + documentPath;
            }

            String fileName = file.getName().toLowerCase();

            // Extract content based on file type
            if (fileName.endsWith(".pdf")) {
                return extractPdfContent(file);
            } else if (fileName.endsWith(".docx")) {
                return extractDocxContent(file);
            } else if (fileName.endsWith(".doc")) {
                return extractDocContent(file);
            } else {
                // Default to text file
                return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return "Error reading document: " + e.getMessage();
        }
    }

    private String extractPdfContent(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocxContent(File docxFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(docxFile))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private String extractDocContent(File docFile) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(docFile))) {
            HWPFDocument document = new HWPFDocument(fs);
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }
}
