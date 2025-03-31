package com.example.courseservice.controller;


import com.example.courseservice.dto.AssignmentSubmissionsDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.AssignmentSubmissionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Slf4j
public class AssignmentSubmissionsController {

    @Autowired
    private AssignmentSubmissionsService assignmentSubmissionsService;

    @GetMapping("/getAssignmentSubmissions")
    public ResponseEntity<ResultDTO> getAssignmentSubmissions(@RequestParam("assignmentId") Long assignmentId,
                                                              @RequestHeader("X-User-Id") String validateUserId) {
        return assignmentSubmissionsService.getAssignmentSubmissions(validateUserId, assignmentId);
    }
    @GetMapping("/downloadSubmissionFile")
    public ResponseEntity<?> downloadSubmissionFile(@RequestParam("submissionId") Long submissionId,
                                                    @RequestParam("fileIndex") Integer fileIndex,
                                                    @RequestHeader("X-User-Id") String userId) {
        return assignmentSubmissionsService.downloadSubmissionFile(submissionId, fileIndex, userId);
    }

    @GetMapping("/getAssignmentSubmissionByUserIdAndAssignmentId")
    public ResponseEntity<ResultDTO> getAssignmentSubmissionByUserIdAndAssignmentId(@RequestParam("userId") String userId,
                                                                                    @RequestParam("assignmentId") Long assignmentId,
                                                                                    @RequestHeader("X-User-Id") String validateUserId){
        return assignmentSubmissionsService.getAssignmentSubmissionByUserIdAndAssignmentId(userId, assignmentId, validateUserId);
    }

    @PutMapping("/updateSubmissionGrade")
    public ResponseEntity<ResultDTO> updateSubmissionGrade(@RequestBody AssignmentSubmissionsDTO dto,
                                                         @RequestHeader("X-User-Id") String userId) {
        return assignmentSubmissionsService.updateSubmissionGrade(dto, userId);
    }

    @PostMapping("/addAssignmentSubmissionText")
    public ResponseEntity<ResultDTO> addAssignmentSubmissionText(@RequestBody AssignmentSubmissionsDTO assignmentSubmissionsDTO,
                                                                 @RequestHeader("X-User-Id") String userId) {
        return assignmentSubmissionsService.addAssignmentSubmissionText(assignmentSubmissionsDTO, userId);
    }

    @PostMapping("/addAssignmentSubmissionFiles")
    public ResponseEntity<ResultDTO> addAssignmentSubmissionFile(@ModelAttribute AssignmentSubmissionsDTO assignmentSubmissionsDTO,
                                                                 MultipartFile[] files,
                                                                 @RequestHeader("X-User-Id") String validateUserId) {
        return assignmentSubmissionsService.addAssignmentSubmissionFiles(assignmentSubmissionsDTO, validateUserId, files);
    }
}
