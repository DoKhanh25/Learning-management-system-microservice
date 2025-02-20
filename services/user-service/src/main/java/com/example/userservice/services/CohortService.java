package com.example.userservice.services;

import com.example.userservice.dto.CohortDTO;
import com.example.userservice.dto.ResultDTO;
import com.example.userservice.entity.CohortEntity;
import com.example.userservice.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CohortService {
    @Autowired
    private CohortRepository cohortRepository;

    public ResponseEntity<ResultDTO> getAllCohorts(){
        ResultDTO resultDTO = new ResultDTO();
        List<CohortEntity> cohortEntityList = cohortRepository.findAll();
        resultDTO.setStatus(1);
        resultDTO.setData(cohortEntityList);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> getCohortById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CohortEntity> optionalCohort = cohortRepository.findById(id);
        if(optionalCohort.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }

        resultDTO.setStatus(1);
        resultDTO.setData(optionalCohort.get());
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> addCohort(CohortDTO cohortDTO){
        ResultDTO resultDTO = new ResultDTO();

        CohortEntity cohortEntity = new CohortEntity();
        cohortEntity.setAvailable((short) 1);
        cohortEntity.setName(cohortDTO.getName());
        cohortEntity.setDescription(cohortDTO.getDescription());
        cohortEntity.setCreatedTime(new Date());
        cohortEntity.setIdNumber(cohortDTO.getIdNumber());
        cohortEntity.setContextId(cohortDTO.getContextId());

        Optional<CohortEntity> cohort = cohortRepository.findCohortEntityByName(cohortDTO.getName());
        if(cohort.isPresent()){
            resultDTO.setStatus(2);
            resultDTO.setData(null);
            resultDTO.setMessage("already exist");
            return ResponseEntity.ok(resultDTO);
        }

        CohortEntity cohortResult = cohortRepository.save(cohortEntity);
        resultDTO.setStatus(1);
        resultDTO.setData(cohortResult);

        return ResponseEntity.ok(resultDTO);
    }

}
