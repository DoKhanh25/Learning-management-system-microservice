package com.example.userservice.controller;

import com.example.userservice.PermissionUtils;
import com.example.userservice.dto.CohortPostDTO;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.services.CohortMemberService;
import com.example.userservice.services.CohortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class CohortController {


    @Autowired
    CohortService cohortService;

    @Autowired
    CohortMemberService cohortMemberService;

    @GetMapping("/getAllCohorts")
    public ResponseEntity<ResultDTO> getAllCohorts(
            @RequestHeader("X-Resource-Scopes") String headers) {
        if(!PermissionUtils.canView("cohort-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return cohortService.getAllCohorts();
    }

    @GetMapping("/getCohortById")
    public ResponseEntity<ResultDTO> getCohortById(@RequestParam Long id,
                                                   @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canView("cohort-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return cohortService.getCohortById(id);
    }

    @PostMapping("/addCohort")
    public ResponseEntity<ResultDTO> addCohort(@RequestBody CohortPostDTO cohortPostDTO,
                                               @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canCreate("cohort-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return cohortService.addCohort(cohortPostDTO);
    }

    @PostMapping("/updateCohort")
    public ResponseEntity<ResultDTO> updateCohort(@RequestBody CohortPostDTO cohortPostDTO,
                                                  @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canEdit("cohort-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return cohortService.updateCohort(cohortPostDTO);
    }

    @PostMapping("/deleteCohorts")
    public ResponseEntity<ResultDTO> deleteCohorts(@RequestBody List<Long> ids,
                                                   @RequestHeader("X-Resource-Scopes") String headers){
        if(!PermissionUtils.canDelete("cohort-management", headers)){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        };
        return cohortService.deleteCohorts(ids);
    }

    @GetMapping("/getCohortMemberEntitiesByCohortId")
    public ResponseEntity<ResultDTO> getCohortMemberEntitiesByCohortId(@RequestParam Long id){
        return cohortMemberService.getCohortMemberEntitiesByCohortId(id);
    }


}
