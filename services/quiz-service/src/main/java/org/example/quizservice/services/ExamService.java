package org.example.quizservice.services;

import com.example.commondto.dto.ResultDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.quizservice.dto.ExamDTO;
import org.example.quizservice.dto.ExamQuestionDTO;
import org.example.quizservice.entity.ExamEntity;
import org.example.quizservice.entity.ExamQuestionEntity;
import org.example.quizservice.entity.QuestionBankEntity;
import org.example.quizservice.entity.QuestionEntity;
import org.example.quizservice.feign.CourseServiceClient;
import org.example.quizservice.mapper.ExamMapper;
import org.example.quizservice.mapper.ExamQuestionMapper;
import org.example.quizservice.repository.ExamQuestionRepository;
import org.example.quizservice.repository.ExamRepository;
import org.example.quizservice.repository.QuestionBankRepository;
import org.example.quizservice.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    
    @Autowired
    private ExamMapper examMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Autowired
    private CourseServiceClient courseServiceClient;

    public ResponseEntity<ResultDTO> getAllExamsByCourseId(Long courseId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(courseId, validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to view exams for this course");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        List<ExamEntity> exams = examRepository.findByCourseId(courseId);
        List<ExamDTO> examDTOs = examMapper.toDto(exams);
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Exams retrieved successfully");
        resultDTO.setData(examDTOs);
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    public ResponseEntity<ResultDTO> getExamById(Long examId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        ExamEntity exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(exam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to view this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        ExamDTO examDTO = examMapper.toDto(exam);
        
        // Get exam questions
        List<ExamQuestionEntity> examQuestions = examQuestionRepository.findByExamId(examId);
        
        List<ExamQuestionDTO> examQuestionDTOs = examQuestionMapper.toDto(examQuestions);
        examDTO.setQuestions(examQuestionDTOs);
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Exam retrieved successfully");
        resultDTO.setData(examDTO);
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> createExam(ExamDTO examDTO, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(examDTO.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to create exams for this course");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Set creation time
        examDTO.setCreatedTime(new Date());
        
        // Create exam entity
        ExamEntity examEntity = examMapper.toEntity(examDTO);
        examEntity = examRepository.save(examEntity);
        
        // Questions should only be added from question banks, not directly
        // Any questions in the DTO will be ignored, as per updated requirements
        
        // Return the created exam
        ExamDTO createdExamDTO = examMapper.toDto(examEntity);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Exam created successfully. Questions can be added from question banks.");
        resultDTO.setData(createdExamDTO);
        
        return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> updateExam(Long examId, ExamDTO examDTO, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Find the exam
        ExamEntity existingExam = examRepository.findById(examId).orElse(null);
        if (existingExam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(existingExam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to update this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Update the exam
        existingExam.setName(examDTO.getName());
        existingExam.setDescription(examDTO.getDescription());
        existingExam.setExamType(examDTO.getExamType());
        existingExam.setDuration(examDTO.getDuration());
        existingExam.setTotalScore(examDTO.getTotalScore());
        existingExam.setNumberQuestions(examDTO.getNumberQuestions());
        existingExam.setShuffleQuestions(examDTO.getShuffleQuestions());
        existingExam.setShuffleAnswers(examDTO.getShuffleAnswers());
        existingExam.setStartTime(examDTO.getStartTime());
        existingExam.setEndTime(examDTO.getEndTime());
        
        existingExam = examRepository.save(existingExam);
        
        ExamDTO updatedExamDTO = examMapper.toDto(existingExam);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Exam updated successfully");
        resultDTO.setData(updatedExamDTO);
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> deleteExam(Long examId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Find the exam
        ExamEntity exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(exam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to delete this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Delete exam questions first
        List<ExamQuestionEntity> examQuestions = examQuestionRepository.findByExamId(examId);
        examQuestionRepository.deleteAll(examQuestions);
        
        // Delete the exam
        examRepository.delete(exam);
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Exam deleted successfully");
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> addQuestionsFromQuestionBank(Long examId, Long questionBankId, 
                                                                 Integer numberOfQuestions, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Find the exam
        ExamEntity exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(exam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to add questions to this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Find the question bank
        QuestionBankEntity questionBank = questionBankRepository.findById(questionBankId).orElse(null);
        if (questionBank == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question bank not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate that question bank type matches exam type
        if (questionBank.getQuestionType() != null && exam.getExamType() != null && 
            !questionBank.getQuestionType().toString().equals(exam.getExamType().toString())) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question bank type does not match exam type");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Get existing exam questions count
        List<ExamQuestionEntity> existingExamQuestions = examQuestionRepository.findByExamId(examId);
        int currentQuestionCount = existingExamQuestions.size();
        
        // Check if adding questions would exceed the exam's question limit
        if (exam.getNumberQuestions() != null && currentQuestionCount + numberOfQuestions > exam.getNumberQuestions()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Cannot add " + numberOfQuestions + " questions. The exam is limited to " + 
                              exam.getNumberQuestions() + " questions and already contains " + currentQuestionCount + " questions.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Get questions from question bank
        List<QuestionEntity> bankQuestions = questionRepository.findQuestionEntitiesByQuestionBankId(questionBankId);
        
        // Validate number of questions
        if (bankQuestions.size() < numberOfQuestions) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question bank contains only " + bankQuestions.size() + 
                               " questions, but " + numberOfQuestions + " were requested");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Check how many questions from this bank are already in the exam
        Integer existingQuestionsCount = examQuestionRepository.countQuestionsByExamIdAndQuestionBankId(examId, questionBankId);
        
        if (existingQuestionsCount + numberOfQuestions > bankQuestions.size()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Cannot add " + numberOfQuestions + " more questions. " +
                               "The exam already contains " + existingQuestionsCount + 
                               " questions from this bank, which has a total of " + bankQuestions.size() + " questions.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Get existing question IDs to avoid duplicates
        Set<Long> existingQuestionIds = existingExamQuestions.stream()
                .map(q -> q.getQuestion().getId())
                .collect(Collectors.toSet());
        
        // Filter available questions (not already in exam)
        List<QuestionEntity> availableQuestions = bankQuestions.stream()
                .filter(q -> !existingQuestionIds.contains(q.getId()))
                .collect(Collectors.toList());
        
        // Shuffle and select the specified number of questions
        Collections.shuffle(availableQuestions);
        List<QuestionEntity> selectedQuestions = availableQuestions.subList(0, 
                                                Math.min(numberOfQuestions, availableQuestions.size()));
        
        // Get next question order
        int nextOrder = existingExamQuestions.isEmpty() ? 1 :
                        existingExamQuestions.stream()
                        .mapToInt(ExamQuestionEntity::getQuestionOrder)
                        .max()
                        .orElse(0) + 1;
        
        // Create exam questions
        List<ExamQuestionEntity> newExamQuestions = new ArrayList<>();
        
        for (QuestionEntity question : selectedQuestions) {
            ExamQuestionEntity examQuestion = new ExamQuestionEntity();
            examQuestion.setExam(exam);
            examQuestion.setQuestion(question);
            examQuestion.setQuestionOrder(nextOrder++);
            examQuestion.setPoints(1.0f); // Default points, can be adjusted later
            
            newExamQuestions.add(examQuestion);
        }
        
        examQuestionRepository.saveAll(newExamQuestions);
        
        // Update total score if needed
        if (exam.getTotalScore() == null) {
            float totalPoints = newExamQuestions.stream().map(ExamQuestionEntity::getPoints).reduce(0.0f, Float::sum);
            totalPoints += existingExamQuestions.stream().map(ExamQuestionEntity::getPoints).reduce(0.0f, Float::sum);
            exam.setTotalScore(totalPoints);
            examRepository.save(exam);
        }
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Added " + newExamQuestions.size() + " questions from question bank to exam");
        resultDTO.setData(examQuestionMapper.toDto(newExamQuestions));
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> addQuestionsFromMultipleQuestionBanks(Long examId, List<Long> questionBankIds, 
                                                                           Integer numberOfQuestions, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Find the exam
        ExamEntity exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(exam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to add questions to this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        if (questionBankIds == null || questionBankIds.isEmpty()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("No question banks specified");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Get existing exam questions
        List<ExamQuestionEntity> existingExamQuestions = examQuestionRepository.findByExamId(examId);
        int currentQuestionCount = existingExamQuestions.size();
        
        // Check if adding questions would exceed the exam's question limit
        if (exam.getNumberQuestions() != null && currentQuestionCount + numberOfQuestions > exam.getNumberQuestions()) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Cannot add " + numberOfQuestions + " questions. The exam is limited to " + 
                              exam.getNumberQuestions() + " questions and already contains " + currentQuestionCount + " questions.");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Get existing question IDs to avoid duplicates
        Set<Long> existingQuestionIds = existingExamQuestions.stream()
                .map(q -> q.getQuestion().getId())
                .collect(Collectors.toSet());
        
        // Collect all available questions from the specified question banks
        List<QuestionEntity> allAvailableQuestions = new ArrayList<>();
        
        for (Long bankId : questionBankIds) {
            // Find the question bank
            QuestionBankEntity questionBank = questionBankRepository.findById(bankId).orElse(null);
            if (questionBank == null) {
                resultDTO.setStatus(0);
                resultDTO.setMessage("Question bank with ID " + bankId + " not found");
                return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
            }
            
            // Validate that question bank type matches exam type
            if (questionBank.getQuestionType() != null && exam.getExamType() != null && 
                !questionBank.getQuestionType().toString().equals(exam.getExamType().toString())) {
                resultDTO.setStatus(0);
                resultDTO.setMessage("Question bank (ID: " + bankId + ") type does not match exam type");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
            }
            
            // Get questions from this bank
            List<QuestionEntity> bankQuestions = questionRepository.findQuestionEntitiesByQuestionBankId(bankId);
            
            // Filter out questions already in the exam
            List<QuestionEntity> availableQuestionsFromBank = bankQuestions.stream()
                    .filter(q -> !existingQuestionIds.contains(q.getId()))
                    .collect(Collectors.toList());
            
            allAvailableQuestions.addAll(availableQuestionsFromBank);
        }
        
        // Remove duplicates (in case the same question appears in multiple banks)
        List<QuestionEntity> uniqueAvailableQuestions = allAvailableQuestions.stream()
                .distinct()
                .collect(Collectors.toList());
        
        // Validate total number of questions available
        if (uniqueAvailableQuestions.size() < numberOfQuestions) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Not enough unique questions available. Required: " + numberOfQuestions + 
                               ", Available: " + uniqueAvailableQuestions.size());
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Randomly select questions
        Collections.shuffle(uniqueAvailableQuestions);
        List<QuestionEntity> selectedQuestions = uniqueAvailableQuestions.subList(0, numberOfQuestions);
        
        // Get next question order
        int nextOrder = existingExamQuestions.isEmpty() ? 1 :
                        existingExamQuestions.stream()
                        .mapToInt(ExamQuestionEntity::getQuestionOrder)
                        .max()
                        .orElse(0) + 1;
        
        // Create exam questions
        List<ExamQuestionEntity> newExamQuestions = new ArrayList<>();
        
        for (QuestionEntity question : selectedQuestions) {
            ExamQuestionEntity examQuestion = new ExamQuestionEntity();
            examQuestion.setExam(exam);
            examQuestion.setQuestion(question);
            examQuestion.setQuestionOrder(nextOrder++);
            examQuestion.setPoints(1.0f); // Default points, can be adjusted later
            
            newExamQuestions.add(examQuestion);
        }
        
        examQuestionRepository.saveAll(newExamQuestions);
        
        // Update total score if needed
        if (exam.getTotalScore() == null) {
            float totalPoints = newExamQuestions.stream().map(ExamQuestionEntity::getPoints).reduce(0.0f, Float::sum);
            totalPoints += existingExamQuestions.stream().map(ExamQuestionEntity::getPoints).reduce(0.0f, Float::sum);
            exam.setTotalScore(totalPoints);
            examRepository.save(exam);
        }
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Added " + newExamQuestions.size() + " questions from multiple question banks to exam");
        resultDTO.setData(examQuestionMapper.toDto(newExamQuestions));
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> removeQuestionFromExam(Long examId, Long questionId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Find the exam
        ExamEntity exam = examRepository.findById(examId).orElse(null);
        if (exam == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Validate if user is a teacher in the course
        if (!validateIsTeacherInCourse(exam.getCourseId(), validateUserId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to remove questions from this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Find the exam question
        ExamQuestionEntity examQuestion = examQuestionRepository.findByQuestionIdAndExamId(questionId, examId);

        if (examQuestion == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Exam question not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }
        
        // Verify the question belongs to the exam
        if (!examQuestion.getExam().getId().equals(examId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Question does not belong to the specified exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }
        
        // Remove the question
        examQuestionRepository.delete(examQuestion);
        
        resultDTO.setStatus(1);
        resultDTO.setMessage("Question removed from exam successfully");
        
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @CircuitBreaker(name = "course-service", fallbackMethod = "fallbackValidate")
    public Boolean validateIsTeacherInCourse(Long courseId, String userId) {
        ResultDTO resultDTO = courseServiceClient.validateIsTeacherInCourse(courseId, userId);
        return resultDTO.getStatus() == 1;
    }


    public Boolean fallbackValidate(Long courseId, String userId, Throwable t) {
        return false;
    }
}
