package org.example.quizservice.services;

import com.example.commondto.dto.ResultDTO;
import com.github.codeboy.piston4j.api.CodeFile;
import com.github.codeboy.piston4j.api.ExecutionRequest;
import com.github.codeboy.piston4j.api.ExecutionResult;
import com.github.codeboy.piston4j.api.Piston;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codeboy.piston4j.exceptions.PistonException;
import com.github.codeboy.piston4j.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.example.quizservice.dto.*;
import org.example.quizservice.entity.*;
import org.example.quizservice.enums.ExamType;
import org.example.quizservice.enums.QuestionType;
import org.example.quizservice.feign.CourseServiceClient;
import org.example.quizservice.mapper.ExamQuestionMapper;
import org.example.quizservice.mapper.ExamSubmissionMapper;
import org.example.quizservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExamSubmissionService {
    private static final List<PistonDataDTO> SUPPORTED_LANGUAGES = new ArrayList<>(Arrays.asList(
        new PistonDataDTO("javascript", "1.32.3", Arrays.asList("deno-js")),
        new PistonDataDTO("go", "1.16.2", Arrays.asList("go", "golang")),
        new PistonDataDTO("c", "10.2.0", Arrays.asList("gcc")),
        new PistonDataDTO("c++", "10.2.0", Arrays.asList("cpp", "g++")),
        new PistonDataDTO("rust", "1.68.2", Arrays.asList("rs")),
        new PistonDataDTO("python", "3.12.0", Arrays.asList("py", "py3", "python3", "python3.12")),
        new PistonDataDTO("java", "15.0.2", new ArrayList<>())
    ));

    private final int retryLimit = 10;
    private final int retryTime = 500;


    @Autowired
    ExamSubmissionRepository examSubmissionRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    ExamQuestionRepository examQuestionRepository;

    @Autowired
    MultipleChoiceSubmissionRepository multipleChoiceSubmissionRepository;

    @Autowired
    EssaySubmissionRepository essaySubmissionRepository;

    @Autowired
    CodingSubmissionRepository codingSubmissionRepository;

    @Autowired
    QuestionSubmissionRepository questionSubmissionRepository;

    @Autowired
    MultipleChoiceOptionRepository multipleChoiceOptionRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CourseServiceClient courseServiceClient;

    @Autowired
    ExamSubmissionMapper examSubmissionMapper;

    @Autowired
    ExamQuestionMapper examQuestionMapper;

    @Value("${piton.server.url}")
    String pitonServerUrl;

    @Transactional
    public ResponseEntity<ResultDTO> startExam(String userId, Long examId) {
        ResultDTO resultDTO = new ResultDTO();
        ExamEntity exam = examRepository.findById(examId).orElse(null);

        if (exam == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        Date now = new Date();
        if (exam.getStartTime().after(now) || exam.getEndTime().before(now)) {
            resultDTO.setMessage("Exam is not available now");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (!this.validateIsInCourse(exam.getCourseId(), userId)) {
            resultDTO.setMessage("You dont have permission to start exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.CONFLICT);
        }

        Integer submissionCount = examSubmissionRepository.countSubmissionsByUserAndExam(userId, examId);
        Integer maxSubmissions = exam.getNumberSubmission() != null ? exam.getNumberSubmission() : 1;

        if (submissionCount >= maxSubmissions) {
            resultDTO.setMessage("Exam is already submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        // Check if there's an ongoing exam and return it
        Optional<ExamSubmissionEntity> ongoingSubmission = examSubmissionRepository.findOngoingSubmission(userId, examId);
        if (ongoingSubmission.isPresent()) {
            ExamSubmissionEntity ongoingSubmissionEntity = ongoingSubmission.get();

            if (now.getTime() - ongoingSubmissionEntity.getStartTime().getTime() > exam.getDuration() * 60 * 1000) {

                ongoingSubmissionEntity.setSubmissionTime(new Date(ongoingSubmissionEntity.getStartTime().getTime() + exam.getDuration() * 60 * 1000));
                examSubmissionRepository.save(ongoingSubmissionEntity);

                resultDTO.setMessage("Exam is already submitted");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
            }

            ExamSubmissionDTO examSubmissionDTO = examSubmissionMapper.toDto(ongoingSubmissionEntity);
            resultDTO.setStatus(1);
            resultDTO.setData(examSubmissionDTO);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }

        // Create a new exam submission
        ExamSubmissionEntity submission = new ExamSubmissionEntity();
        submission.setUserId(userId);
        submission.setExam(exam);
        submission.setStartTime(now);
        submission.setIsGraded(false);
        submission = examSubmissionRepository.save(submission);

        resultDTO.setStatus(1);
        resultDTO.setData(examSubmissionMapper.toDto(submission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ResultDTO> submitMultipleChoiceAnswer(String userId, MultipleChoiceSubmissionDTO submissionDTO) {
        ResultDTO resultDTO = new ResultDTO();

        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(submissionDTO.getExamSubmissionId()).orElse(null);

        if (examSubmission == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        ExamEntity examEntity = examSubmission.getExam();
        if (!validateIsInCourse(examEntity.getCourseId(), userId)) {
            resultDTO.setMessage("You don't have permission to submit this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to submit this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        QuestionEntity question = questionRepository.findById(submissionDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        Optional<QuestionSubmissionEntity> existingSubmission = questionSubmissionRepository
                .findByExamSubmissionIdAndQuestionId(examSubmission.getId(), question.getId());

        MultipleChoiceSubmissionEntity submission = this.handleMultipleChoiceSubmission(
                examSubmission, question, submissionDTO, existingSubmission);

        resultDTO.setStatus(1);
        resultDTO.setData(convertToQuestionSubmissionDTO(submission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ResultDTO> submitEssayAnswer(String userId, EssaySubmissionDTO submissionDTO) {
        ResultDTO resultDTO = new ResultDTO();
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(submissionDTO.getExamSubmissionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));

        // Verify the user owns this submission
        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to submit this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        QuestionEntity question = questionRepository.findById(submissionDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        Optional<QuestionSubmissionEntity> existingSubmission = questionSubmissionRepository
                .findByExamSubmissionIdAndQuestionId(examSubmission.getId(), question.getId());

        EssaySubmissionEntity submission = handleEssaySubmission(
                examSubmission, question, submissionDTO, existingSubmission);

        resultDTO.setStatus(1);
        resultDTO.setData(convertToQuestionSubmissionDTO(submission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ResultDTO> submitCodingAnswer(String userId, CodingSubmissionDTO submissionDTO) {
        ResultDTO resultDTO = new ResultDTO();
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(submissionDTO.getExamSubmissionId()).orElse(null);

        if (examSubmission == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to submit this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        QuestionEntity question = questionRepository.findById(submissionDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (question instanceof CodingQuestionEntity) {
            CodingQuestionEntity codingQuestion = (CodingQuestionEntity) question;

            // Validate that the submitted language matches the question's required language
//            if (codingQuestion.getProgrammingLanguage() != null &&
//                !codingQuestion.getProgrammingLanguage().equalsIgnoreCase(submissionDTO.getLanguage())) {
//                resultDTO.setStatus(0);
//                resultDTO.setMessage("You must use " + codingQuestion.getProgrammingLanguage() +
//                                     " to answer this question. You submitted code in " + submissionDTO.getLanguage());
//                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
//            }
        }

        Optional<QuestionSubmissionEntity> existingSubmission = questionSubmissionRepository
                .findByExamSubmissionIdAndQuestionId(examSubmission.getId(), question.getId());
        CodingSubmissionEntity submission = handleCodingSubmission(
                examSubmission, question, submissionDTO, existingSubmission);

        resultDTO.setStatus(1);
        resultDTO.setData(convertToQuestionSubmissionDTO(submission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ResultDTO> submitExam(String userId, Long examSubmissionId) {
        ResultDTO resultDTO = new ResultDTO();

        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(examSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));

        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to submit this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        // Set submission time
        examSubmission.setSubmissionTime(new Date());
        calculateMultipleChoiceScores(examSubmission);
        examSubmission = examSubmissionRepository.save(examSubmission);

        resultDTO.setStatus(1);
        resultDTO.setData(convertToDTO(examSubmission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    private void calculateMultipleChoiceScores(ExamSubmissionEntity examSubmission) {
        float totalScore = 0;
        boolean allGraded = true;

        List<QuestionSubmissionEntity> submissions = questionSubmissionRepository
                .findByExamSubmissionId(examSubmission.getId());

        for (QuestionSubmissionEntity submission : submissions) {
            if (submission instanceof MultipleChoiceSubmissionEntity) {
                MultipleChoiceSubmissionEntity mcSubmission = (MultipleChoiceSubmissionEntity) submission;

                QuestionEntity question = mcSubmission.getQuestion();

                ExamQuestionEntity examQuestion = examQuestionRepository
                        .findByExamIdAndQuestionId(examSubmission.getExam().getId(), question.getId())
                        .orElse(null);

                Float questionPoints = (examQuestion != null) ? examQuestion.getPoints() : question.getPoints();

                if (question instanceof MultipleChoiceQuestionEntity) {
                    MultipleChoiceQuestionEntity mcQuestion = (MultipleChoiceQuestionEntity) question;

                    List<MultipleChoiceOptionEntity> options = multipleChoiceOptionRepository
                            .findByQuestionId(question.getId());

                    List<Long> correctOptionIds = options.stream()
                            .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                            .map(MultipleChoiceOptionEntity::getId)
                            .collect(Collectors.toList());

                    List<Long> selectedOptionIds = mcSubmission.getSelectedOptionIds();

                    // Calculate score based on correct answers
                    float score = calculateMultipleChoiceScore(correctOptionIds, selectedOptionIds,
                            mcQuestion.getAllowMultipleAnswers(), questionPoints);

                    mcSubmission.setScore(score);
                    mcSubmission.setGraded(true);
                    questionSubmissionRepository.save(mcSubmission);

                    totalScore += score;
                }
            } else {
                allGraded = false;
            }
        }

        examSubmission.setTotalScore(totalScore);
        examSubmission.setIsGraded(allGraded);

        if (allGraded) {
            examSubmission.setGradingTime(new Date());
        }
    }

    private float calculateMultipleChoiceScore(List<Long> correctOptionIds, List<Long> selectedOptionIds,
                                               Boolean allowMultipleAnswers, Float questionPoints) {
        if (questionPoints == null) {
            questionPoints = 1.0f;
        }

        if (correctOptionIds.isEmpty() || selectedOptionIds == null) {
            return 0;
        }

        if (Boolean.FALSE.equals(allowMultipleAnswers)) {
            if (selectedOptionIds.size() == 1 && correctOptionIds.size() == 1 &&
                    selectedOptionIds.get(0).equals(correctOptionIds.get(0))) {
                return questionPoints;
            }
            return 0;
        } else {

            if (selectedOptionIds.containsAll(correctOptionIds) &&
                    correctOptionIds.containsAll(selectedOptionIds)) {
                return questionPoints;
            }

            return 0;
        }
    }

    public ResponseEntity<ResultDTO> getExamSubmission(String userId, Long submissionId) {
        ResultDTO resultDTO = new ResultDTO();
        ExamSubmissionEntity submission = examSubmissionRepository.findById(submissionId).orElse(null);

        if (submission == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        ExamEntity examEntity = examRepository.findById(submission.getExam().getId()).orElse(null);
        if (examEntity == null) {
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if (!submission.getUserId().equals(userId) && !(validateTeacherInCourse(examEntity.getCourseId(), userId))) {
            resultDTO.setMessage("You don't have permission to view this submission");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        resultDTO.setStatus(1);
        resultDTO.setData(convertToDTO(submission));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> getExamSubmissions(String userId, Long examId) {
        ResultDTO resultDTO = new ResultDTO();

        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));

        Boolean isTeacher = this.validateTeacherInCourse(exam.getCourseId(), userId);
        if (!isTeacher) {
            resultDTO.setMessage("You don't have permission to view this submission");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        // Get all submissions for this exam
        List<ExamSubmissionEntity> submissions = examSubmissionRepository.findAll().stream()
                .filter(sub -> sub.getExam().getId().equals(examId))
                .collect(Collectors.toList());

        List<ExamSubmissionDTO> submissionDTOs = submissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        resultDTO.setStatus(1);
        resultDTO.setData(submissionDTOs);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }


    public ResponseEntity<ResultDTO> getExamQuestions(String userId, Long examSubmissionId) {
        ResultDTO resultDTO = new ResultDTO();

        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(examSubmissionId).orElse(null);

        if (examSubmission == null) {
            resultDTO.setMessage("Exam submission not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to view this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        Date now = new Date();
        ExamEntity exam = examSubmission.getExam();

        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        if (now.getTime() - examSubmission.getStartTime().getTime() > exam.getDuration() * 60 * 1000) {
            examSubmission.setSubmissionTime(new Date(examSubmission.getStartTime().getTime() + exam.getDuration() * 60 * 1000));
            examSubmissionRepository.save(examSubmission);

            resultDTO.setMessage("Exam time has expired and it has been automatically submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        List<ExamQuestionEntity> examQuestions = examQuestionRepository.findByExamId(exam.getId());

        if (Boolean.TRUE.equals(exam.getShuffleQuestions())) {
            Collections.shuffle(examQuestions);
        } else {
            examQuestions.sort(Comparator.comparing(ExamQuestionEntity::getQuestionOrder));
        }

        List<ExamQuestionDTO> questionDTOs = examQuestions.stream()
                .map(examQuestionMapper::toDto)
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(exam.getShuffleAnswers())) {
            shuffleAnswerOptions(questionDTOs);
        }

        List<QuestionEntity> questionEntities = new ArrayList<>();

        // Fetch all question entities
        for (ExamQuestionDTO e : questionDTOs) {
            QuestionEntity question = questionRepository.findById(e.getQuestionId()).orElse(null);
            if (question != null) {
                questionEntities.add(question);
            }
        }

        List<Object> examQuestionDTOs = new ArrayList<>();

        for (QuestionEntity q : questionEntities) {
            QuestionType questionType = q.getQuestionType();

            if (QuestionType.MULTIPLE_CHOICE.equals(questionType)) {
                MultipleChoiceQuestionDTO mcQuestionDTO = new MultipleChoiceQuestionDTO();
                mcQuestionDTO.setId(q.getId());
                mcQuestionDTO.setQuestionType(questionType);
                mcQuestionDTO.setText(q.getText());

                if (q instanceof MultipleChoiceQuestionEntity) {
                    mcQuestionDTO.setAllowMultipleAnswers(((MultipleChoiceQuestionEntity) q).getAllowMultipleAnswers());
                }

                List<MultipleChoiceOptionEntity> optionEntities = multipleChoiceOptionRepository.findByQuestionId(q.getId());
                List<MultipleChoiceOptionDTO> optionDTOs = new ArrayList<>();

                for (MultipleChoiceOptionEntity optionEntity : optionEntities) {
                    MultipleChoiceOptionDTO optionDTO = new MultipleChoiceOptionDTO();
                    optionDTO.setId(optionEntity.getId());
                    optionDTO.setText(optionEntity.getText());
                    optionDTO.setQuestionId(q.getId());
                    optionDTOs.add(optionDTO);
                }
                mcQuestionDTO.setOptions(optionDTOs);
                examQuestionDTOs.add(mcQuestionDTO);
            } else if (QuestionType.ESSAY.equals(questionType)) {
                // Create Essay question DTO
                EssayQuestionDTO essayQuestionDTO = new EssayQuestionDTO();
                essayQuestionDTO.setId(q.getId());
                essayQuestionDTO.setQuestionType(questionType);
                essayQuestionDTO.setText(q.getText());
                // Add other essay specific properties if needed
                examQuestionDTOs.add(essayQuestionDTO);
            } else if (QuestionType.CODING.equals(questionType)) {
                CodingQuestionEntity codingQuestionEntity = (CodingQuestionEntity) q;
                // Create Coding question DTO
                CodingQuestionDTO codingQuestionDTO = new CodingQuestionDTO();
                codingQuestionDTO.setId(q.getId());
                codingQuestionDTO.setQuestionType(questionType);
                codingQuestionDTO.setText(q.getText());

                codingQuestionDTO.setStarterCode(codingQuestionEntity.getStarterCode());
                codingQuestionDTO.setProgrammingLanguage(codingQuestionEntity.getProgrammingLanguage());
                codingQuestionDTO.setTestCases(codingQuestionEntity.getTestCases());

                examQuestionDTOs.add(codingQuestionDTO);
            }
        }

        resultDTO.setStatus(1);
        resultDTO.setData(examQuestionDTOs);
        resultDTO.setMessage("Exam questions retrieved successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> getQuestionSubmissions(Long examSubmissionId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(examSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));

        ExamEntity exam = examSubmission.getExam();

        Boolean isTeacher = this.validateTeacherInCourse(exam.getCourseId(), validateUserId);

        if(!examSubmission.getUserId().equals(validateUserId)) {
            if (!isTeacher || examSubmission.getSubmissionTime() == null) {
                resultDTO.setMessage("You don't have permission to view this submission");
                return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
            }
        }

        List<QuestionSubmissionEntity> questionSubmissions = questionSubmissionRepository.findByExamSubmissionId(examSubmissionId);

        if(exam.getExamType() == ExamType.MULTIPLE_CHOICE) {
            List<MultipleChoiceSubmissionEntity> list = questionSubmissions
                    .stream()
                    .map(e -> (MultipleChoiceSubmissionEntity) e)
                    .toList();

            resultDTO.setData(list);
            resultDTO.setStatus(1);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);

        } else if(exam.getExamType() == ExamType.ESSAY) {
            List<EssaySubmissionEntity> list = questionSubmissions
                    .stream()
                    .map(e -> (EssaySubmissionEntity) e)
                    .toList();

            resultDTO.setData(list);
            resultDTO.setStatus(1);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);
        }

        List<CodingSubmissionEntity> list = questionSubmissions
                .stream()
                .map(e -> (CodingSubmissionEntity) e)
                .toList();

        resultDTO.setData(list);
        resultDTO.setStatus(1);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    private void shuffleAnswerOptions(List<ExamQuestionDTO> questionDTOs) {
        for (ExamQuestionDTO questionDTO : questionDTOs) {
            // Load the actual question entity
            QuestionEntity questionEntity = questionRepository.findById(questionDTO.getQuestionId()).orElse(null);
            if (questionEntity instanceof MultipleChoiceQuestionEntity) {
                MultipleChoiceQuestionEntity mcQuestion = (MultipleChoiceQuestionEntity) questionEntity;
                List<MultipleChoiceOptionEntity> options = new ArrayList<>(mcQuestion.getOptions());

                Collections.shuffle(options);

                for (int i = 0; i < options.size(); i++) {
                    options.get(i).setDisplayOrder(i);
                }
            }
        }
    }

    private MultipleChoiceSubmissionEntity handleMultipleChoiceSubmission(
            ExamSubmissionEntity examSubmission,
            QuestionEntity question,
            MultipleChoiceSubmissionDTO dto,
            Optional<QuestionSubmissionEntity> existingSubmission) {

        MultipleChoiceSubmissionEntity submission;

        if (existingSubmission.isPresent()) {
            submission = (MultipleChoiceSubmissionEntity) existingSubmission.get();
        } else {
            submission = new MultipleChoiceSubmissionEntity();
            submission.setExamSubmission(examSubmission);
            submission.setQuestion(question);
            submission.setGraded(false);
        }

        submission.setSelectedOptionIds(dto.getSelectedOptionIds());
        submission.setSubmittedAt(new Date());

        return multipleChoiceSubmissionRepository.save(submission);
    }

    private EssaySubmissionEntity handleEssaySubmission(
            ExamSubmissionEntity examSubmission,
            QuestionEntity question,
            EssaySubmissionDTO dto,
            Optional<QuestionSubmissionEntity> existingSubmission) {

        EssaySubmissionEntity submission;

        if (existingSubmission.isPresent()) {
            submission = (EssaySubmissionEntity) existingSubmission.get();
        } else {
            submission = new EssaySubmissionEntity();
            submission.setExamSubmission(examSubmission);
            submission.setQuestion(question);
            submission.setGraded(false);
        }

        submission.setAnswerText(dto.getAnswerText());
        submission.setWordCount(dto.getWordCount());
        submission.setSubmittedAt(new Date());

        return essaySubmissionRepository.save(submission);
    }

    private CodingSubmissionEntity handleCodingSubmission(
            ExamSubmissionEntity examSubmission,
            QuestionEntity question,
            CodingSubmissionDTO dto,
            Optional<QuestionSubmissionEntity> existingSubmission) {

        CodingSubmissionEntity submission;

        if (existingSubmission.isPresent()) {
            submission = (CodingSubmissionEntity) existingSubmission.get();
        } else {
            submission = new CodingSubmissionEntity();
            submission.setExamSubmission(examSubmission);
            submission.setQuestion(question);
            submission.setGraded(false);
        }

        submission.setSubmittedCode(dto.getSubmittedCode());
        submission.setTestResults(dto.getTestResults());
        submission.setCompilationOutput(dto.getCompilationOutput());
        submission.setExecutionTime(dto.getExecutionTime());
        submission.setSubmittedAt(new Date());

        return codingSubmissionRepository.save(submission);
    }

    private ExamSubmissionDTO convertToDTO(ExamSubmissionEntity entity) {
        ExamSubmissionDTO dto = new ExamSubmissionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setExamId(entity.getExam().getId());
        dto.setStartTime(entity.getStartTime());
        dto.setSubmissionTime(entity.getSubmissionTime());
        dto.setTotalScore(entity.getTotalScore());
        dto.setIsGraded(entity.getIsGraded());
        dto.setGradedBy(entity.getGradedBy());
        dto.setGradingTime(entity.getGradingTime());
        dto.setFeedback(entity.getFeedback());

        if (entity.getQuestionSubmissions() != null) {
            dto.setQuestionSubmissions(entity.getQuestionSubmissions().stream()
                    .map(this::convertToQuestionSubmissionDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QuestionSubmissionDTO convertToQuestionSubmissionDTO(QuestionSubmissionEntity entity) {
        if (entity instanceof MultipleChoiceSubmissionEntity) {
            MultipleChoiceSubmissionDTO dto = new MultipleChoiceSubmissionDTO();
            dto.setSelectedOptionIds(((MultipleChoiceSubmissionEntity) entity).getSelectedOptionIds());
            setCommonFields(dto, entity);
            return dto;
        } else if (entity instanceof EssaySubmissionEntity) {
            EssaySubmissionDTO dto = new EssaySubmissionDTO();
            dto.setAnswerText(((EssaySubmissionEntity) entity).getAnswerText());
            dto.setWordCount(((EssaySubmissionEntity) entity).getWordCount());
            setCommonFields(dto, entity);
            return dto;
        } else if (entity instanceof CodingSubmissionEntity) {
            CodingSubmissionDTO dto = new CodingSubmissionDTO();
            dto.setSubmittedCode(((CodingSubmissionEntity) entity).getSubmittedCode());
            dto.setTestResults(((CodingSubmissionEntity) entity).getTestResults());
            dto.setCompilationOutput(((CodingSubmissionEntity) entity).getCompilationOutput());
            dto.setExecutionTime(((CodingSubmissionEntity) entity).getExecutionTime());
            setCommonFields(dto, entity);
            return dto;
        }

        // Fallback for unknown types
        QuestionSubmissionDTO dto = new QuestionSubmissionDTO();
        setCommonFields(dto, entity);
        return dto;
    }

    @Transactional
    public ResponseEntity<ResultDTO> codeExecute(String userId, CodeExecutionRequestDTO requestDTO) {
        ResultDTO resultDTO = new ResultDTO();

        // Validate that the user has access to this submission
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(requestDTO.getExamSubmissionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));

        if (!examSubmission.getUserId().equals(userId)) {
            resultDTO.setMessage("You don't have permission to execute code for this submission");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        // Check if exam is already submitted
        if (examSubmission.getSubmissionTime() != null) {
            resultDTO.setMessage("This exam has already been submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        // Get the question
        QuestionEntity question = questionRepository.findById(requestDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (!(question instanceof CodingQuestionEntity)) {
            resultDTO.setMessage("This is not a coding question");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        CodingQuestionEntity codingQuestion = (CodingQuestionEntity) question;

        // Validate that the submitted language matches the question's required language
        if (codingQuestion.getProgrammingLanguage() != null &&
            !codingQuestion.getProgrammingLanguage().equalsIgnoreCase(requestDTO.getLanguage())) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You must use " + codingQuestion.getProgrammingLanguage() +
                                 " to answer this question. You submitted code in " + requestDTO.getLanguage());
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            // Initialize Piston API
            Piston api = Piston.getInstance(pitonServerUrl);

            // Create execution request

            String version = SUPPORTED_LANGUAGES.stream().filter(lang -> lang.getLanguage().equals(requestDTO.getLanguage()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported language"))
                    .getVersion();

            List<TestCaseDTO> testCaseDTOs = new ArrayList<>();
            if (codingQuestion.getTestCases() != null && !codingQuestion.getTestCases().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                testCaseDTOs = mapper.readValue(codingQuestion.getTestCases(),
                        new TypeReference<List<TestCaseDTO>>() {});
            }

            // If no test cases found and no stdin provided, return error
            if (testCaseDTOs.isEmpty() && (requestDTO.getStdin() == null || requestDTO.getStdin().isEmpty())) {
                resultDTO.setMessage("No input data available to execute code");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
            }

            // Create response with execution results
            Map<String, Object> response = new HashMap<>();
            boolean allTestsPassed = true;
            List<Map<String, Object>> testResults = new ArrayList<>();

            // If test cases exist, run each test case
            if (!testCaseDTOs.isEmpty()) {
                for (TestCaseDTO testCase : testCaseDTOs) {
                    CodeFile codeFile = new CodeFile(requestDTO.getStdin());

                    ExecutionRequest executionRequest = new ExecutionRequest(requestDTO.getLanguage(), version, codeFile);
                    executionRequest.setStdin(testCase.getInput());

                    ExecutionResult result = this.execute(executionRequest);

                    boolean testPassed = testCase.getExpectedOutput() != null &&
                            result.getOutput().getOutput().contains(testCase.getExpectedOutput().trim());

                    if (!testPassed) {
                        allTestsPassed = false;
                    }

                    Map<String, Object> testResult = new HashMap<>();
                    testResult.put("description", testCase.getDescription());
                    testResult.put("input", testCase.getInput());
                    testResult.put("expectedOutput", testCase.getExpectedOutput());
                    testResult.put("actualOutput", result.getOutput().getOutput());
                    testResult.put("passed", testPassed);
                    testResults.add(testResult);
                }

                response.put("testResults", testResults);
            } else {
                // If no test cases, just run with provided stdin
                CodeFile codeFile = new CodeFile(requestDTO.getStdin());
                ExecutionRequest executionRequest = new ExecutionRequest(requestDTO.getLanguage(), version, codeFile);

                // Execute code
                ExecutionResult result = this.execute(executionRequest);

                response.put("stdout", result.getOutput().getStdout());
                response.put("stderr", result.getOutput().getStderr());
                response.put("output", result.getOutput().getOutput());
            }

            response.put("testsPassed", allTestsPassed);

            resultDTO.setStatus(1);
            resultDTO.setData(response);
            return new ResponseEntity<>(resultDTO, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error executing code: ", e);
            resultDTO.setMessage("Error executing code: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean evaluateTestCases(String testCasesJson, String actualOutput) {
        try {
            if (testCasesJson == null || testCasesJson.isEmpty()) {
                return true; // No test cases to evaluate
            }

            // Parse test cases JSON
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> testCases = mapper.readValue(testCasesJson,
                    new TypeReference<List<Map<String, String>>>() {});

            for (Map<String, String> testCase : testCases) {
                String expectedOutput = testCase.get("expectedOutput");

                if (expectedOutput != null && !actualOutput.contains(expectedOutput.trim())) {
                    log.debug("Test case failed: {} - Expected '{}' but got '{}'",
                             testCase.get("description"), expectedOutput, actualOutput);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error evaluating test cases: ", e);
            // If parsing fails, return false
            return false;
        }
    }

    private boolean evaluateTestCasesWithDTO(List<TestCaseDTO> testCases, String actualOutput) {
        if (testCases == null || testCases.isEmpty()) {
            return true; // No test cases to evaluate
        }

        ObjectMapper mapper = new ObjectMapper();

        for (TestCaseDTO testCase : testCases) {
            String expectedOutput = testCase.getExpectedOutput();
            if (expectedOutput == null) continue;

            try {
                // Check if both expected and actual outputs are arrays
                if ((expectedOutput.trim().startsWith("[") && expectedOutput.trim().endsWith("]")) &&
                        (actualOutput.trim().startsWith("[") && actualOutput.trim().endsWith("]"))) {

                    // Parse arrays
                    int[] expectedArray = mapper.readValue(expectedOutput, int[].class);
                    int[] actualArray = mapper.readValue(actualOutput, int[].class);

                    // Sort arrays
                    Arrays.sort(expectedArray);
                    Arrays.sort(actualArray);

                    // Compare sorted arrays
                    if (!Arrays.equals(expectedArray, actualArray)) {
                        log.debug("Test case failed: {} - Expected array '{}' but got '{}'",
                                testCase.getDescription(),
                                Arrays.toString(expectedArray),
                                Arrays.toString(actualArray));
                        return false;
                    }
                } else {
                    // Use original string comparison for non-array data
                    if (!actualOutput.contains(expectedOutput.trim())) {
                        log.debug("Test case failed: {} - Expected '{}' but got '{}'",
                                testCase.getDescription(), expectedOutput, actualOutput);
                        return false;
                    }
                }
            } catch (Exception e) {
                // If parsing fails, fall back to string comparison
                if (!actualOutput.contains(expectedOutput.trim())) {
                    log.debug("Test case failed: {} - Expected '{}' but got '{}'",
                            testCase.getDescription(), expectedOutput, actualOutput);
                    return false;
                }
            }
        }
        return true;
    }

    private void setCommonFields(QuestionSubmissionDTO dto, QuestionSubmissionEntity entity) {
        dto.setId(entity.getId());
        dto.setExamSubmissionId(entity.getExamSubmission().getId());
        dto.setQuestionId(entity.getQuestion().getId());
        dto.setScore(entity.getScore());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setGraded(entity.getGraded());
        dto.setFeedback(entity.getFeedback());
    }

    @CircuitBreaker(name = "course-service", fallbackMethod = "fallbackValidate")
    public Boolean validateIsInCourse(Long courseId, String userId) {
        ResultDTO resultDTO = courseServiceClient.validateIsCourse(courseId, userId);
        if (resultDTO.getStatus() == 1) {
            return true;
        }
        return false;
    }

    @CircuitBreaker(name = "course-service", fallbackMethod = "fallbackValidate")
    public Boolean validateTeacherInCourse(Long courseId, String userId) {
        ResultDTO resultDTO = courseServiceClient.validateIsTeacherInCourse(courseId, userId);
        if (resultDTO.getStatus() == 1) {
            return true;
        }
        return false;
    }

    public Boolean fallbackValidate(Long courseId, String userId, Throwable t) {
        return false;
    }

    public ResponseEntity<ResultDTO> getSupportedLanguages() {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(1);
        resultDTO.setData(SUPPORTED_LANGUAGES);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ExecutionResult execute(ExecutionRequest request) {
        String apiKey = null;
        if (!request.isValid())
            throw new IllegalArgumentException("Request invalid");
        HttpURLConnection con;
        try {
            URL url = new URL(this.pitonServerUrl + "/execute");
            int retries = -1;
            do {
                retries++;
                if (retries >= retryLimit) {
                    throw new PistonException("Reached retry limit (" + retryLimit + ")");
                }



                Thread.sleep((long) retries * retryTime);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (apiKey != null)
                    con.addRequestProperty("Authorization", apiKey);
                con.addRequestProperty("Content-Type", "application/" + "json");
                con.setDoOutput(true);
                con.setRequestProperty("User-Agent", "Piston4J");

                String requestBody = new Gson().toJson(request);
                con.setRequestProperty("Content-Length", Integer.toString(requestBody.length()));
                con.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            } while (con.getResponseCode() == 429);
        } catch (IOException | InterruptedException e) {
            throw new PistonException(e);
        }


        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            String result = stringBuilder.toString();
            return new Gson().fromJson(result, ExecutionResult.class);
        } catch (IOException e) {
            InputStream errorStream = con.getErrorStream();
            if (errorStream != null) {
                String error = Util.readStream(errorStream);
                JsonObject object = new Gson().fromJson(error, JsonObject.class);
                String message = error;
                if (object.has("message"))
                    message = object.get("message").getAsString();
                throw new PistonException(message);
            }
            throw new PistonException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> gradeEssayQuestion(String userId, Map<String, Object> requestBody) {
        ResultDTO resultDTO = new ResultDTO();
        
        Long submissionId = Long.valueOf(requestBody.get("submissionId").toString());
        Long questionSubmissionId = Long.valueOf(requestBody.get("questionSubmissionId").toString());
        Float score = Float.valueOf(requestBody.get("score").toString());
        String feedback = (String) requestBody.get("feedback");
        
        // Verify the submission exists
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));
        
        // Verify the user is a teacher for this course
        if (!validateTeacherInCourse(examSubmission.getExam().getCourseId(), userId)) {
            resultDTO.setMessage("You don't have permission to grade this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Get the question submission
        EssaySubmissionEntity essaySubmission = (EssaySubmissionEntity) questionSubmissionRepository.findById(questionSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question submission not found"));
        
        // Update the grade
        essaySubmission.setScore(score);
        essaySubmission.setFeedback(feedback);
        essaySubmission.setGraded(true);
        essaySubmission = (EssaySubmissionEntity) questionSubmissionRepository.save(essaySubmission);
        
        // Update the total score of the exam submission
        updateExamSubmissionTotalScore(examSubmission);
        
        resultDTO.setStatus(1);
        resultDTO.setData(convertToQuestionSubmissionDTO(essaySubmission));
        resultDTO.setMessage("Essay graded successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> gradeCodingQuestion(String userId, Map<String, Object> requestBody) {
        ResultDTO resultDTO = new ResultDTO();
        
        Long submissionId = Long.valueOf(requestBody.get("submissionId").toString());
        Long questionSubmissionId = Long.valueOf(requestBody.get("questionSubmissionId").toString());
        Float score = Float.valueOf(requestBody.get("score").toString());
        String feedback = (String) requestBody.get("feedback");
        
        // Verify the submission exists
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));
        
        // Verify the user is a teacher for this course
        if (!validateTeacherInCourse(examSubmission.getExam().getCourseId(), userId)) {
            resultDTO.setMessage("You don't have permission to grade this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Get the question submission
        CodingSubmissionEntity codingSubmission = (CodingSubmissionEntity) questionSubmissionRepository.findById(questionSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question submission not found"));
        
        // Update the grade
        codingSubmission.setScore(score);
        codingSubmission.setFeedback(feedback);
        codingSubmission.setGraded(true);
        codingSubmission = (CodingSubmissionEntity) questionSubmissionRepository.save(codingSubmission);
        
        // Update the total score of the exam submission
        updateExamSubmissionTotalScore(examSubmission);
        
        resultDTO.setStatus(1);
        resultDTO.setData(convertToQuestionSubmissionDTO(codingSubmission));
        resultDTO.setMessage("Coding question graded successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    @Transactional
    public ResponseEntity<ResultDTO> finalizeGrading(String userId, Long examSubmissionId) {
        ResultDTO resultDTO = new ResultDTO();
        
        // Verify the submission exists
        ExamSubmissionEntity examSubmission = examSubmissionRepository.findById(examSubmissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam submission not found"));
        
        // Verify the user is a teacher for this course
        if (!validateTeacherInCourse(examSubmission.getExam().getCourseId(), userId)) {
            resultDTO.setMessage("You don't have permission to grade this exam");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }
        
        // Update the total score and mark as graded
        updateExamSubmissionTotalScore(examSubmission);
        examSubmission.setIsGraded(true);
        examSubmission.setGradingTime(new Date());
        examSubmission = examSubmissionRepository.save(examSubmission);
        
        resultDTO.setStatus(1);
        resultDTO.setData(convertToDTO(examSubmission));
        resultDTO.setMessage("Grading finalized successfully");
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }
    
    private void updateExamSubmissionTotalScore(ExamSubmissionEntity examSubmission) {
        List<QuestionSubmissionEntity> submissions = questionSubmissionRepository
                .findByExamSubmissionId(examSubmission.getId());
        
        float totalScore = 0;
        boolean allGraded = true;
        
        for (QuestionSubmissionEntity submission : submissions) {
            if (submission.getGraded() && submission.getScore() != null) {
                totalScore += submission.getScore();
            } else {
                allGraded = false;
            }
        }
        
        examSubmission.setTotalScore(totalScore);
        examSubmission.setIsGraded(allGraded);
        
        if (allGraded) {
            examSubmission.setGradingTime(new Date());
        }
        
        examSubmissionRepository.save(examSubmission);
    }
}
