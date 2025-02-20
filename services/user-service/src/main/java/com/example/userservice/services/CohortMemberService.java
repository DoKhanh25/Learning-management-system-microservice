package com.example.userservice.services;

import com.example.userservice.dto.ResultDTO;
import com.example.userservice.entity.CohortEntity;
import com.example.userservice.entity.CohortMemberEntity;
import com.example.userservice.repository.CohortMemberRepository;
import com.example.userservice.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CohortMemberService {
    @Autowired
    CohortMemberRepository cohortMemberRepository;

    @Autowired
    CohortRepository cohortRepository;

    public ResponseEntity<ResultDTO> addCohortMembersToCohort(List<String> keycloakIds, Long cohortId){
        Optional<CohortEntity> cohortEntityOptional = cohortRepository.findById(cohortId);
        ResultDTO resultDTO = new ResultDTO();

        if(cohortEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Dont exist");
            return ResponseEntity.ok(resultDTO);
        }
        CohortEntity cohort = cohortEntityOptional.get();
        List<CohortMemberEntity> cohortMemberEntityList = new ArrayList<>();
        for (String id: keycloakIds){
            CohortMemberEntity cohortMemberEntity = new CohortMemberEntity();
            cohortMemberEntity.setCohort(cohort);
            cohortMemberEntity.setAvailable((short) 1);
            cohortMemberEntity.setAddedTime(new Date());
            cohortMemberEntity.setKeycloakId(id);
            cohortMemberEntityList.add(cohortMemberEntity);
        }

        List<CohortMemberEntity> cohortMemberListResult = cohortMemberRepository.saveAll(cohortMemberEntityList);
        resultDTO.setData(cohortMemberListResult);
        resultDTO.setStatus(1);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> removeCohortMembersInCohort(List<Long> cohortMemberIds){
        ResultDTO resultDTO = new ResultDTO();
        cohortMemberRepository.deleteAllById(cohortMemberIds);
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }

}
