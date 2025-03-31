package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.AssignmentSubmissionsDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.*;
import com.example.courseservice.enums.AssignmentType;
import com.example.courseservice.enums.CourseRole;
import com.example.courseservice.mapper.AssignmentSubmissionsMapper;
import com.example.courseservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AssignmentSubmissionsService {
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    AssignmentSubmissionsRepository assignmentSubmissionsRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    AssignmentSubmissionsMapper assignmentSubmissionsMapper;

    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    @Autowired
    FilesRepository filesRepository;

    public ResponseEntity<ResultDTO> getAssignmentSubmissions(String userId, Long assignmentId) {
        ResultDTO resultDTO = new ResultDTO();

        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId).orElse(null);

        if(assignmentEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        Long courseId = assignmentEntity.getCourse().getId();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(enrolEntity.getCourseRole() == CourseRole.STUDENT){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        List<AssignmentSubmissionsEntity> assignmentSubmissionsEntities = assignmentSubmissionsRepository.findAllByAssignmentId(assignmentId);
        List<AssignmentSubmissionsDTO> assignmentSubmissionsDTOs = assignmentSubmissionsMapper.toDtoList(assignmentSubmissionsEntities, new CycleAvoidingMappingContext());

        resultDTO.setStatus(1);
        resultDTO.setData(assignmentSubmissionsDTOs);
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> downloadSubmissionFile(Long submissionId, Integer fileIndex, String userId) {
        ResultDTO resultDTO = new ResultDTO();

        // Find the submission
        AssignmentSubmissionsEntity submission = assignmentSubmissionsRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Submission not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        // Check permissions (user is either the owner or a teacher)
        Long courseId = submission.getAssignment().getCourse().getId();
        EnrolEntity enrol = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if (enrol == null && !submission.getUserId().equals(userId)) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to access this file");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        // Get file paths and names
        String[] filePaths = submission.getData2().split(";");
        String[] fileNames = submission.getData1().split(";");

        if (fileIndex < 0 || fileIndex >= filePaths.length) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Invalid file index");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        // Get the file
        String filePath = filePaths[fileIndex];
        String fileName = fileNames[fileIndex];

        try {
            Path path = Paths.get(filePath);
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            String contentType = determineContentType(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(Files.size(path))
                    .body(resource);
        } catch (IOException e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Error retrieving file: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResultDTO> getAssignmentSubmissionByUserIdAndAssignmentId(String userId, Long assignmentId, String validateUserId) {
        ResultDTO resultDTO = new ResultDTO();

        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId).orElse(null);

        if(assignmentEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        Long courseId = assignmentEntity.getCourse().getId();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(validateUserId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(enrolEntity.getCourseRole() == CourseRole.STUDENT && !validateUserId.equals(userId)){
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        AssignmentSubmissionsEntity assignmentSubmissionsEntity = assignmentSubmissionsRepository.findByUserIdAndAssignmentId(userId, assignmentId);

        if(assignmentSubmissionsEntity == null) {
            resultDTO.setStatus(1);
            resultDTO.setMessage("Assignment submit not found");
            return ResponseEntity.ok(resultDTO);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(assignmentSubmissionsMapper.toDto(assignmentSubmissionsEntity, new CycleAvoidingMappingContext()));
        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResultDTO> addAssignmentSubmissionText(AssignmentSubmissionsDTO assignmentSubmissionsDTO, String userId) {
        ResultDTO resultDTO = new ResultDTO();
        Long assignmentId = assignmentSubmissionsDTO.getAssignmentId();

        // validate user in course
        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId).orElse(null);

        if(assignmentEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        Long courseId = assignmentEntity.getCourse().getId();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(assignmentEntity.getAssignmentType() != AssignmentType.TEXT){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment type not supported");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        // check resubmit and prevent_late

        boolean isPreventLate = assignmentEntity.getPreventLate();
        boolean resubmit = assignmentEntity.getResubmit();

        if(assignmentEntity.getStartDate().after(new Date())) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You cannot submit this assignment yet");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }


        if (isPreventLate && assignmentEntity.getEndDate().before(new Date())) {
                resultDTO.setStatus(0);
                resultDTO.setMessage("Assignment is late");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AssignmentSubmissionsEntity assignmentSubmissionsEntity = assignmentSubmissionsRepository.findByUserIdAndAssignmentId(userId, assignmentId);

        if (assignmentSubmissionsEntity == null) {
            AssignmentSubmissionsEntity assignmentSubmissions = assignmentSubmissionsMapper.toEntity(assignmentSubmissionsDTO, new CycleAvoidingMappingContext());
            assignmentSubmissions.setAssignment(assignmentEntity);
            assignmentSubmissions.setCreatedTime(new Date());
            assignmentSubmissions.setUserId(userId);
            assignmentSubmissions = assignmentSubmissionsRepository.save(assignmentSubmissions);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Assignment submitted");
            resultDTO.setData(assignmentSubmissionsMapper.toDto(assignmentSubmissions, new CycleAvoidingMappingContext()));
            return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);

        } else if (!resubmit) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You can not resubmit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        } else {
            assignmentSubmissionsEntity.setData1(assignmentSubmissionsDTO.getData1());
            assignmentSubmissionsEntity.setUpdatedTime(new Date());
            assignmentSubmissionsEntity = assignmentSubmissionsRepository.save(assignmentSubmissionsEntity);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Assignment submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
        }
    }

    public ResponseEntity<ResultDTO> updateSubmissionGrade(AssignmentSubmissionsDTO dto, String userId) {
        ResultDTO resultDTO = new ResultDTO();

        // Find the submission
        AssignmentSubmissionsEntity submission = assignmentSubmissionsRepository.findById(dto.getId()).orElse(null);
        if (submission == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Submission not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        // Verify user is a teacher for this course
        Long courseId = submission.getAssignment().getCourse().getId();
        EnrolEntity enrol = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if (enrol == null || enrol.getCourseRole() == CourseRole.STUDENT) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You don't have permission to grade submissions");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        // Update the grade and comment
        submission.setGrade(dto.getGrade());
        submission.setSubmissionComment(dto.getSubmissionComment());
        submission = assignmentSubmissionsRepository.save(submission);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Grade updated successfully");
        resultDTO.setData(assignmentSubmissionsMapper.toDto(submission, new CycleAvoidingMappingContext()));

        return new ResponseEntity<>(resultDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<ResultDTO> addAssignmentSubmissionFiles(AssignmentSubmissionsDTO assignmentSubmissionsDTO,
                                                                String userId,
                                                                MultipartFile[] files) {
        ResultDTO resultDTO = new ResultDTO();
        Long assignmentId = assignmentSubmissionsDTO.getAssignmentId();

        // validate user in course
        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId).orElse(null);

        if(assignmentEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment not found");
            return new ResponseEntity<>(resultDTO, HttpStatus.NOT_FOUND);
        }

        Long courseId = assignmentEntity.getCourse().getId();
        EnrolEntity enrolEntity = userEnrolmentsRepository.getEnrolEntityByUserId(userId, courseId);

        if(enrolEntity == null) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You dont have permission to submit this assignment");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(assignmentEntity.getAssignmentType() != AssignmentType.FILE){
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment type not supported");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if(files == null || files.length == 0) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("No files submitted");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        for (MultipartFile file : files) {
            if (!isValidFileType(file.getOriginalFilename())) {
                resultDTO.setStatus(0);
                resultDTO.setMessage("Invalid file type. Only PDF, Word, PowerPoint, and Excel files are allowed.");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
            }
        }

        boolean isPreventLate = assignmentEntity.getPreventLate();
        boolean resubmit = assignmentEntity.getResubmit();

        if(assignmentEntity.getStartDate().after(new Date())) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("You cannot submit this assignment yet");
            return new ResponseEntity<>(resultDTO, HttpStatus.FORBIDDEN);
        }

        if (isPreventLate && assignmentEntity.getEndDate().before(new Date())) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Assignment is late");
            return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
        }

        AssignmentSubmissionsEntity assignmentSubmissionsEntity = assignmentSubmissionsRepository.findByUserIdAndAssignmentId(userId, assignmentId);

        try {
            StringBuilder filenamesBuilder = new StringBuilder();
            StringBuilder filePathsBuilder = new StringBuilder();


            if (assignmentSubmissionsEntity != null && resubmit) {
                // Get previously submitted file paths
                String oldFilePaths = assignmentSubmissionsEntity.getData2();
                if (oldFilePaths != null && !oldFilePaths.isEmpty()) {
                    String[] oldPathsArray = oldFilePaths.split(";");

                    // Delete old files
                    for (String oldPath : oldPathsArray) {
                        try {
                            Path fileToDelete = Paths.get(oldPath);
                            Files.deleteIfExists(fileToDelete);

                            // Remove from database
                            filesRepository.deleteFilesEntityByFilePath(oldPath);
                        } catch (IOException e) {
                            // Log error but continue with submission
                            System.err.println("Error deleting old file: " + oldPath + " - " + e.getMessage());
                        }
                    }
                }
            }


            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String uploadDir = "uploads/assignments";
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = saveFile(uploadDir, fileName, file);

                FilesEntity filesEntity = new FilesEntity();
                filesEntity.setFileName(file.getOriginalFilename());
                filesEntity.setFileSize(String.valueOf(file.getSize()));
                filesEntity.setFilePath(filePath);
                filesEntity.setAuthor(userId);

                filesRepository.save(filesEntity);

                filenamesBuilder.append(file.getOriginalFilename());
                filePathsBuilder.append(filePath);

                if (i < files.length - 1) {
                    filePathsBuilder.append(";");
                    filenamesBuilder.append(";");
                }

            }

            String filePaths = filePathsBuilder.toString();
            String filenames = filenamesBuilder.toString();

            if (assignmentSubmissionsEntity == null) {
                AssignmentSubmissionsEntity assignmentSubmissions = assignmentSubmissionsMapper.toEntity(assignmentSubmissionsDTO, new CycleAvoidingMappingContext());
                assignmentSubmissions.setAssignment(assignmentEntity);
                assignmentSubmissions.setCreatedTime(new Date());
                assignmentSubmissions.setUserId(userId);
                assignmentSubmissions.setData1(filenames);
                assignmentSubmissions.setData2(filePaths);
                assignmentSubmissions = assignmentSubmissionsRepository.save(assignmentSubmissions);


                resultDTO.setStatus(1);
                resultDTO.setMessage("Assignment submitted");
                resultDTO.setData(assignmentSubmissionsMapper.toDto(assignmentSubmissions, new CycleAvoidingMappingContext()));
                return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
            } else if (!resubmit) {
                resultDTO.setStatus(0);
                resultDTO.setMessage("You cannot resubmit this assignment");
                return new ResponseEntity<>(resultDTO, HttpStatus.BAD_REQUEST);
            } else {
                assignmentSubmissionsEntity.setData1(filenames);
                assignmentSubmissionsEntity.setData2(filePaths);

                assignmentSubmissionsEntity.setUpdatedTime(new Date());
                assignmentSubmissionsEntity = assignmentSubmissionsRepository.save(assignmentSubmissionsEntity);


                resultDTO.setStatus(1);
                resultDTO.setMessage("Assignment submitted");
                resultDTO.setData(assignmentSubmissionsEntity);

                return new ResponseEntity<>(resultDTO, HttpStatus.CREATED);
            }
        } catch (IOException e) {
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to upload files: " + e.getMessage());
            return new ResponseEntity<>(resultDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String saveFile(String uploadDir, String fileName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + "/" + fileName;
    }

    private String determineContentType(String filename) {
        if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".docx") || filename.endsWith(".doc")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (filename.endsWith(".pptx") || filename.endsWith(".ppt")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            throw new IllegalArgumentException("Invalid file type. Only PDF, Word, PowerPoint, and Excel files are allowed.");
        }
    }

    private boolean isValidFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".pdf") ||
                lowerCaseFilename.endsWith(".doc") ||
                lowerCaseFilename.endsWith(".docx") ||
                lowerCaseFilename.endsWith(".ppt") ||
                lowerCaseFilename.endsWith(".pptx") ||
                lowerCaseFilename.endsWith(".xls") ||
                lowerCaseFilename.endsWith(".xlsx");
    }

}
