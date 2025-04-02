package org.example.quizservice.services;

import com.example.commondto.dto.ResultDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.poi.ss.usermodel.*;
import org.example.quizservice.dto.CodingQuestionDTO;
import org.example.quizservice.dto.EssayQuestionDTO;
import org.example.quizservice.dto.MultipleChoiceOptionDTO;
import org.example.quizservice.dto.MultipleChoiceQuestionDTO;
import org.example.quizservice.entity.*;
import org.example.quizservice.enums.QuestionType;
import org.example.quizservice.feign.CourseServiceClient;
import org.example.quizservice.mapper.CodingQuestionMapper;
import org.example.quizservice.mapper.EssayQuestionMapper;
import org.example.quizservice.mapper.MultipleChoiceQuestionMapper;
import org.example.quizservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private EssayQuestionRepository essayQuestionRepository;

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private MultipleChoiceOptionRepository multipleChoiceOptionRepository;
    @Autowired
    private MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

    @Autowired
    MultipleChoiceQuestionMapper multipleChoiceQuestionMapper;

    @Autowired
    EssayQuestionMapper essayQuestionMapper;

    @Autowired
    CodingQuestionMapper codingQuestionMapper;

    @Autowired
    CourseServiceClient courseServiceClient;



    public ResponseEntity<ResultDTO> findQuestionEntitiesByQuestionBankId(Long questionBankId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        QuestionBankEntity questionBankEntity = questionBankRepository.findById(questionBankId).orElse(null);

        if(questionBankEntity == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(!validateIsTeacherInCourse(questionBankEntity.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(questionBankEntity.getQuestionType() == QuestionType.MULTIPLE_CHOICE){
            List<MultipleChoiceQuestionEntity> multipleChoiceQuestionEntities = multipleChoiceQuestionRepository.findMultipleChoiceQuestionsByQuestionBankId(questionBankId);
            List<MultipleChoiceQuestionDTO> multipleChoiceQuestionDTOList = multipleChoiceQuestionMapper.toDto(multipleChoiceQuestionEntities);

            resultDTO.setStatus(1);
            resultDTO.setData(multipleChoiceQuestionDTOList);

            return new ResponseEntity<>(resultDTO, HttpStatus.OK);

        } else if(questionBankEntity.getQuestionType() == QuestionType.ESSAY){
            List<EssayQuestionEntity> essayQuestionEntities = essayQuestionRepository.findEssayQuestionEntitiesByQuestionBankId(questionBankId);
            List<EssayQuestionDTO> essayQuestionDTOList = essayQuestionMapper.toDto(essayQuestionEntities);

            resultDTO.setStatus(1);
            resultDTO.setData(essayQuestionDTOList);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } else {
            List<CodingQuestionEntity> codingQuestionEntities = codingQuestionRepository.findCodingQuestionEntitiesByQuestionBankId(questionBankId);
            List<CodingQuestionDTO> codingQuestionDTOList = codingQuestionMapper.toDto(codingQuestionEntities);

            resultDTO.setStatus(1);
            resultDTO.setData(codingQuestionDTOList);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }

    }


    @Transactional
    public ResponseEntity<ResultDTO> addMultipleChoiceQuestion(MultipleChoiceQuestionDTO questionDTO, String validateUserId) {
        // Validate permission
//        ResponseEntity<ResultDTO> validationResult = validateUserPermission(questionDTO.getC(), validateUserId);



        QuestionBankEntity questionBank = questionBankRepository.findById(questionDTO.getQuestionBankId()).orElse(null);
        ResultDTO resultDTO = new ResultDTO();

        if(questionBank == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(!validateIsTeacherInCourse(questionBank.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (questionDTO.getAllowMultipleAnswers() && questionDTO.getOptions().stream().filter(MultipleChoiceOptionDTO::getIsCorrect).count() < 2) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Multiple correct answers are required when multiple answers are allowed.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }


        MultipleChoiceQuestionEntity entity = new MultipleChoiceQuestionEntity();
        entity.setText(questionDTO.getText());
        entity.setPoints(questionDTO.getPoints());
        entity.setDifficultyLevel(questionDTO.getDifficultyLevel());
        entity.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        entity.setUserId(validateUserId);
        entity.setQuestionBank(questionBank);
        entity.setAllowMultipleAnswers(questionDTO.getAllowMultipleAnswers());

        MultipleChoiceQuestionEntity savedQuestion = multipleChoiceQuestionRepository.save(entity);

        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            List<MultipleChoiceOptionEntity> optionEntities = new ArrayList<>();
            for (var optionDTO : questionDTO.getOptions()) {
                MultipleChoiceOptionEntity optionEntity = new MultipleChoiceOptionEntity();
                optionEntity.setText(optionDTO.getText());
                optionEntity.setIsCorrect(optionDTO.getIsCorrect());
                optionEntity.setDisplayOrder(optionDTO.getDisplayOrder());
                optionEntity.setQuestion(savedQuestion);
                optionEntities.add(optionEntity);
            }
            savedQuestion.setOptions(optionEntities);
            savedQuestion = multipleChoiceQuestionRepository.save(savedQuestion);
        }

        resultDTO.setStatus(1);
        resultDTO.setMessage("Multiple choice question added successfully");
        resultDTO.setData(savedQuestion);
        return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
    }
    
    public ResponseEntity<ResultDTO> addCodingQuestion(CodingQuestionDTO questionDTO, String validateUserId) {
        // Validate permission
//        ResponseEntity<ResultDTO> validationResult = validateQuestionBankAccess(questionDTO.getQuestionBankId(), validateUserId);
//        if (validationResult != null) {
//            return validationResult;
//        }

        QuestionBankEntity questionBank = questionBankRepository.findById(questionDTO.getQuestionBankId()).orElse(null);
        ResultDTO resultDTO = new ResultDTO();

        if(questionBank == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(!validateIsTeacherInCourse(questionBank.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        CodingQuestionEntity entity = new CodingQuestionEntity();
        entity.setText(questionDTO.getText());
        entity.setPoints(questionDTO.getPoints());
        entity.setDifficultyLevel(questionDTO.getDifficultyLevel());
        entity.setQuestionType(QuestionType.CODING);
        entity.setUserId(validateUserId);
        entity.setQuestionBank(questionBank);
        entity.setProgrammingLanguage(questionDTO.getProgrammingLanguage());
        entity.setStarterCode(questionDTO.getStarterCode());
        entity.setSolutionCode(questionDTO.getSolutionCode());
        entity.setTestCases(questionDTO.getTestCases());

        CodingQuestionEntity savedQuestion = codingQuestionRepository.save(entity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Coding question added successfully");
        resultDTO.setData(savedQuestion);
        return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
    }
    
    public ResponseEntity<ResultDTO> addEssayQuestion(EssayQuestionDTO questionDTO, String validateUserId) {
        // Validate permission
//        ResponseEntity<ResultDTO> validationResult = validateQuestionBankAccess(questionDTO.getQuestionBankId(), validateUserId);
//        if (validationResult != null) {
//            return validationResult;
//        }

        QuestionBankEntity questionBank = questionBankRepository.findById(questionDTO.getQuestionBankId()).orElse(null);
        ResultDTO resultDTO = new ResultDTO();

        if(questionBank == null){
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if(!validateIsTeacherInCourse(questionBank.getCourseId(), validateUserId)){
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        EssayQuestionEntity entity = new EssayQuestionEntity();
        entity.setText(questionDTO.getText());
        entity.setPoints(questionDTO.getPoints());
        entity.setDifficultyLevel(questionDTO.getDifficultyLevel());
        entity.setQuestionType(QuestionType.ESSAY);
        entity.setUserId(validateUserId);
        entity.setQuestionBank(questionBank);

        EssayQuestionEntity savedQuestion = essayQuestionRepository.save(entity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Essay question added successfully");
        resultDTO.setData(savedQuestion);
        return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
    }


    @Transactional
    public ResponseEntity<ResultDTO> addMultipleEssayQuestionsFromExcel(MultipartFile excelFile, String validateUserId, Long questionBankId) {
        ResultDTO resultDTO = new ResultDTO();

        // Validate question bank access
        QuestionBankEntity questionBank = questionBankRepository.findById(questionBankId).orElse(null);
        if (questionBank == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question bank not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if (!validateIsTeacherInCourse(questionBank.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to add questions to this question bank");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        try {
            // Process Excel file
            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<EssayQuestionEntity> savedQuestions = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Read cells - adjust indices based on your Excel structure
                    String text = getCellValueAsString(row.getCell(0));
                    int points = (int) row.getCell(1).getNumericCellValue();
                    String difficultyLevel = getCellValueAsString(row.getCell(2));

                    // Validate required fields
                    if (text == null || text.trim().isEmpty()) {
                        errors.add("Row " + (i+1) + ": Question text is required");
                        continue;
                    }

                    // Create and save entity
                    EssayQuestionEntity entity = new EssayQuestionEntity();
                    entity.setText(text);
                    entity.setPoints(points);
                    entity.setDifficultyLevel(difficultyLevel);
                    entity.setQuestionType(QuestionType.ESSAY);
                    entity.setUserId(validateUserId);
                    entity.setQuestionBank(questionBank);

                    EssayQuestionEntity savedQuestion = essayQuestionRepository.save(entity);
                    savedQuestions.add(savedQuestion);
                } catch (Exception e) {
                    errors.add("Error processing row " + (i+1) + ": " + e.getMessage());
                }
            }

            workbook.close();

            // Prepare result
            resultDTO.setStatus(1);
            resultDTO.setMessage("Processed " + savedQuestions.size() + " essay questions" +
                                (errors.isEmpty() ? "" : " with " + errors.size() + " errors"));

            Map<String, Object> data = new HashMap<>();
            data.put("successCount", savedQuestions.size());
            data.put("errors", errors);
            data.put("questions", savedQuestions);
            resultDTO.setData(data);

            return new ResponseEntity<>(resultDTO,
                    errors.isEmpty() ? HttpStatus.CREATED : HttpStatus.PARTIAL_CONTENT);

        } catch (Exception e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to process Excel file: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> addMultipleChoiceQuestionsFromExcel(MultipartFile excelFile, String validateUserId, Long questionBankId) {
        ResultDTO resultDTO = new ResultDTO();

        // Validate question bank access
        QuestionBankEntity questionBank = questionBankRepository.findById(questionBankId).orElse(null);
        if (questionBank == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question bank not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if (!validateIsTeacherInCourse(questionBank.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to add questions to this question bank");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        try {
            // Process Excel file
            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<MultipleChoiceQuestionEntity> savedQuestions = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Read main question fields
                    String text = getCellValueAsString(row.getCell(0));
                    int points = (int) row.getCell(1).getNumericCellValue();
                    String difficultyLevel = getCellValueAsString(row.getCell(2));
                    boolean allowMultipleAnswers = "yes".equalsIgnoreCase(getCellValueAsString(row.getCell(3)));

                    // Validate required fields
                    if (text == null || text.trim().isEmpty()) {
                        errors.add("Row " + (i+1) + ": Question text is required");
                        continue;
                    }

                    // Create question entity
                    MultipleChoiceQuestionEntity questionEntity = new MultipleChoiceQuestionEntity();
                    questionEntity.setText(text);
                    questionEntity.setPoints(points);
                    questionEntity.setDifficultyLevel(difficultyLevel);
                    questionEntity.setQuestionType(QuestionType.MULTIPLE_CHOICE);
                    questionEntity.setUserId(validateUserId);
                    questionEntity.setQuestionBank(questionBank);
                    questionEntity.setAllowMultipleAnswers(allowMultipleAnswers);

                    // Process options (starting from column 4)
                    List<MultipleChoiceOptionEntity> optionEntities = new ArrayList<>();
                    int optionCount = 0;
                    int correctCount = 0;

                    // Read options - each option takes 2 columns (text, isCorrect)
                    for (int j = 0; j < 5; j++) { // Assuming max 5 options
                        int optionTextColIndex = 4 + (j * 2);
                        int optionCorrectColIndex = 5 + (j * 2);
                        
                        if (optionTextColIndex >= row.getLastCellNum()) break;
                        
                        String optionText = getCellValueAsString(row.getCell(optionTextColIndex));
                        if (optionText == null || optionText.trim().isEmpty()) continue;
                        
                        boolean isCorrect = "yes".equalsIgnoreCase(getCellValueAsString(row.getCell(optionCorrectColIndex)));
                        if (isCorrect) correctCount++;
                        
                        MultipleChoiceOptionEntity optionEntity = new MultipleChoiceOptionEntity();
                        optionEntity.setText(optionText);
                        optionEntity.setIsCorrect(isCorrect);
                        optionEntity.setDisplayOrder(j);
                        optionEntity.setQuestion(questionEntity);
                        optionEntities.add(optionEntity);
                        optionCount++;
                    }

                    // Validate options
                    if (optionCount < 2) {
                        errors.add("Row " + (i+1) + ": Question must have at least 2 options");
                        continue;
                    }

                    if (correctCount == 0) {
                        errors.add("Row " + (i+1) + ": Question must have at least 1 correct answer");
                        continue;
                    }

                    if (allowMultipleAnswers && correctCount < 2) {
                        errors.add("Row " + (i+1) + ": Multiple choice questions with multiple answers allowed must have at least 2 correct options");
                        continue;
                    }

                    // Save question and options
                    questionEntity = multipleChoiceQuestionRepository.save(questionEntity);
                    questionEntity.setOptions(optionEntities);
                    multipleChoiceOptionRepository.saveAll(optionEntities);
                    
                    savedQuestions.add(questionEntity);
                } catch (Exception e) {
                    errors.add("Error processing row " + (i+1) + ": " + e.getMessage());
                }
            }

            workbook.close();

            // Prepare result
            resultDTO.setStatus(1);
            resultDTO.setMessage("Processed " + savedQuestions.size() + " multiple choice questions" +
                                (errors.isEmpty() ? "" : " with " + errors.size() + " errors"));

            Map<String, Object> data = new HashMap<>();
            data.put("successCount", savedQuestions.size());
            data.put("errors", errors);
            data.put("questions", savedQuestions);
            resultDTO.setData(data);

            return new ResponseEntity<>(resultDTO,
                    errors.isEmpty() ? HttpStatus.CREATED : HttpStatus.PARTIAL_CONTENT);

        } catch (Exception e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to process Excel file: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> updateMultipleChoiceQuestion(Long questionId, MultipleChoiceQuestionDTO questionDTO, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        // Find the question
        MultipleChoiceQuestionEntity existingQuestion = multipleChoiceQuestionRepository.findById(questionId).orElse(null);
        if (existingQuestion == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        // Validate permission
        if (!validateIsTeacherInCourse(existingQuestion.getQuestionBank().getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to update this question");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (questionDTO.getAllowMultipleAnswers() && questionDTO.getOptions().stream().filter(MultipleChoiceOptionDTO::getIsCorrect).count() < 2) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Multiple correct answers are required when multiple answers are allowed.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            // Update basic fields
            existingQuestion.setText(questionDTO.getText());
            existingQuestion.setPoints(questionDTO.getPoints());
            existingQuestion.setDifficultyLevel(questionDTO.getDifficultyLevel());
            existingQuestion.setAllowMultipleAnswers(questionDTO.getAllowMultipleAnswers());

            // Handle options - remove existing ones and add new ones
            if (existingQuestion.getOptions() != null) {
                existingQuestion.getOptions().clear();
            }

            if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
                List<MultipleChoiceOptionEntity> optionEntities = new ArrayList<>();
                for (var optionDTO : questionDTO.getOptions()) {
                    MultipleChoiceOptionEntity optionEntity = new MultipleChoiceOptionEntity();
                    optionEntity.setText(optionDTO.getText());
                    optionEntity.setIsCorrect(optionDTO.getIsCorrect());
                    optionEntity.setDisplayOrder(optionDTO.getDisplayOrder());
                    optionEntity.setQuestion(existingQuestion);
                    optionEntities.add(optionEntity);
                }
                existingQuestion.getOptions().addAll(optionEntities);
            }

            MultipleChoiceQuestionEntity updatedQuestion = multipleChoiceQuestionRepository.save(existingQuestion);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Multiple choice question updated successfully");
            resultDTO.setData(updatedQuestion);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } catch (Exception e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to update question: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> updateCodingQuestion(Long questionId, CodingQuestionDTO questionDTO, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        // Find the question
        CodingQuestionEntity existingQuestion = codingQuestionRepository.findById(questionId).orElse(null);
        if (existingQuestion == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        // Validate permission
        if (!validateIsTeacherInCourse(existingQuestion.getQuestionBank().getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to update this question");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        try {
            // Update fields
            existingQuestion.setText(questionDTO.getText());
            existingQuestion.setPoints(questionDTO.getPoints());
            existingQuestion.setDifficultyLevel(questionDTO.getDifficultyLevel());
            existingQuestion.setProgrammingLanguage(questionDTO.getProgrammingLanguage());
            existingQuestion.setStarterCode(questionDTO.getStarterCode());
            existingQuestion.setSolutionCode(questionDTO.getSolutionCode());
            existingQuestion.setTestCases(questionDTO.getTestCases());

            CodingQuestionEntity updatedQuestion = codingQuestionRepository.save(existingQuestion);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Coding question updated successfully");
            resultDTO.setData(updatedQuestion);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } catch (Exception e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to update question: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> updateEssayQuestion(Long questionId, EssayQuestionDTO questionDTO, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        // Find the question
        EssayQuestionEntity existingQuestion = essayQuestionRepository.findById(questionId).orElse(null);
        if (existingQuestion == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        // Validate permission
        if (!validateIsTeacherInCourse(existingQuestion.getQuestionBank().getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to update this question");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        try {
            // Update fields
            existingQuestion.setText(questionDTO.getText());
            existingQuestion.setPoints(questionDTO.getPoints());
            existingQuestion.setDifficultyLevel(questionDTO.getDifficultyLevel());

            EssayQuestionEntity updatedQuestion = essayQuestionRepository.save(existingQuestion);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Essay question updated successfully");
            resultDTO.setData(updatedQuestion);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        } catch (Exception e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to update question: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<ResultDTO> deleteQuestion(Long questionId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        QuestionEntity question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        if (!validateIsTeacherInCourse(question.getQuestionBank().getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to delete this question");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        questionRepository.delete(question);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Question deleted successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }


    @CircuitBreaker(name = "course-service", fallbackMethod = "fallbackValidate")
    public Boolean validateIsTeacherInCourse(Long courseId, String userId) {
        ResultDTO resultDTO = courseServiceClient.validateIsTeacherInCourse(courseId, userId);
        if(resultDTO.getStatus() == 1){
            return true;
        }
        return false;
    }

    public Boolean fallbackValidate(Long courseId, String userId, Throwable t) {
        return false;
    }
}
