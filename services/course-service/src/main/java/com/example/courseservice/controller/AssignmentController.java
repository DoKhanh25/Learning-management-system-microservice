package com.example.courseservice.controller;


import com.example.courseservice.dto.AIChatSessionDTO;
import com.example.courseservice.dto.AssignmentDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.services.AssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class AssignmentController {
    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/getAllAssignmentsByCourseId")
    public ResponseEntity<ResultDTO> getAllAssignmentsByCourseId(@RequestParam("courseId") Long courseId,
                                                                 @RequestHeader("X-User-Id") String userId){
        return assignmentService.getAllAssignmentsByCourseId(courseId, userId);
    }

    @GetMapping("/getAssignmentById")
    public ResponseEntity<ResultDTO> getAssignmentById(@RequestParam("assignmentId") Long assignmentId,
                                                       @RequestHeader("X-User-Id") String userId){
        return assignmentService.getAssignmentById(assignmentId, userId);
    }


    @PostMapping("/addAssignment")
    public ResponseEntity<ResultDTO> addAssignment(@RequestBody AssignmentDTO assignmentDTO,
                                                  @RequestHeader("X-User-Id") String userId){
        return assignmentService.addAssignment(assignmentDTO,userId);
    }

    @PutMapping("/updateAssignment")
    public ResponseEntity<ResultDTO> updateAssignment(@RequestBody AssignmentDTO assignmentDTO,
                                                      @RequestHeader("X-User-Id") String userId){
        return assignmentService.updateAssignment(assignmentDTO,userId);
    }

    @DeleteMapping("/deleteAssignmentById")
    public ResponseEntity<ResultDTO> deleteAssignmentById(@RequestParam("assignmentId") Long assignmentId,
                                                          @RequestHeader("X-User-Id") String userId){
        return assignmentService.deleteAssignmentById(assignmentId, userId);
    }
}
