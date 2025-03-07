package com.example.userservice.controller;

import com.example.userservice.dto.CohortDTO;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.services.CohortMemberService;
import com.example.userservice.services.CohortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ResultDTO> getAllCohorts(){
        return cohortService.getAllCohorts();
    }

    @GetMapping("/getCohortById")
    public ResponseEntity<ResultDTO> getCohortById(@RequestParam Long id){
        return cohortService.getCohortById(id);
    }

    @PostMapping("/addCohort")
    public ResponseEntity<ResultDTO> addCohort(@RequestBody CohortDTO cohortDTO){
        return cohortService.addCohort(cohortDTO);
    }

    @PostMapping("/updateCohort")
    public ResponseEntity<ResultDTO> updateCohort(@RequestBody CohortDTO cohortDTO){
        return cohortService.updateCohort(cohortDTO);
    }

    @PostMapping("/deleteCohorts")
    public ResponseEntity<ResultDTO> deleteCohorts(@RequestBody List<Long> ids){
        return cohortService.deleteCohorts(ids);
    }

    @GetMapping("/getCohortMemberEntitiesByCohortId")
    public ResponseEntity<ResultDTO> getCohortMemberEntitiesByCohortId(@RequestParam Long id){
        return cohortMemberService.getCohortMemberEntitiesByCohortId(id);
    }


}
