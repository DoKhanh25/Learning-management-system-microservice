package com.example.userservice.services;

import com.example.userservice.dto.CohortDTO;
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
public class CohortService {
    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CohortMemberRepository cohortMemberRepository;

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


        Optional<CohortEntity> cohort = cohortRepository.findCohortEntityByName(cohortDTO.getName());
        if(cohort.isPresent()){
            resultDTO.setStatus(2);
            resultDTO.setData(null);
            resultDTO.setMessage("already exist");
            return ResponseEntity.ok(resultDTO);
        }

        CohortEntity cohortResult = cohortRepository.save(cohortEntity);

        List<CohortMemberEntity> cohortMemberEntityList = new ArrayList<>();
        for (String id: cohortDTO.getUserIds()){
            CohortMemberEntity cohortMemberEntity = new CohortMemberEntity();
            cohortMemberEntity.setCohort(cohortResult);
            cohortMemberEntity.setAvailable((short) 1);
            cohortMemberEntity.setAddedTime(new Date());
            cohortMemberEntity.setKeycloakId(id);
            cohortMemberEntityList.add(cohortMemberEntity);
        }

        List<CohortMemberEntity> cohortMemberListResult = cohortMemberRepository.saveAll(cohortMemberEntityList);

        resultDTO.setStatus(1);
        resultDTO.setData(cohortMemberListResult);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> updateCohort(CohortDTO cohortDTO){
        ResultDTO resultDTO = new ResultDTO();
        Optional<CohortEntity> cohortEntityOptional = cohortRepository.findById(cohortDTO.getId());
        if(cohortEntityOptional.isEmpty()){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Dont exist");
            return ResponseEntity.ok(resultDTO);
        }

        CohortEntity cohort = cohortEntityOptional.get();


        List<CohortMemberEntity> cohortMemberEntityList = cohort.getCohortMembers();
        List<String> existUserIds = new ArrayList<>();
        List<String> updateIds = cohortDTO.getUserIds();

        if(updateIds == null || updateIds.isEmpty()){
            cohort.getCohortMembers().clear();
            cohort.setUpdatedTime(new Date());
            cohort.setAvailable(cohortDTO.getAvailable());
            cohort.setDescription(cohortDTO.getDescription());

            CohortEntity cohortEntityResult = cohortRepository.save(cohort);
            resultDTO.setStatus(1);
            resultDTO.setData(cohortEntityResult);
            return ResponseEntity.ok(resultDTO);
        }

        for(CohortMemberEntity cohortMember: cohortMemberEntityList){
            existUserIds.add(cohortMember.getKeycloakId());
        }

        cohortMemberEntityList.removeIf(child -> updateIds.stream()
                .noneMatch(newId -> newId != null && newId.equals(child.getKeycloakId()))
        );

        for(String updateId: updateIds){
            if(!existUserIds.contains(updateId)){
                CohortMemberEntity cohortMemberEntity = new CohortMemberEntity();
                cohortMemberEntity.setCohort(cohort);
                cohortMemberEntity.setAvailable((short) 1);
                cohortMemberEntity.setAddedTime(new Date());
                cohortMemberEntity.setKeycloakId(updateId);
                cohortMemberEntityList.add(cohortMemberEntity);
            }
        }

        cohort.setCohortMembers(cohortMemberEntityList);
        cohort.setUpdatedTime(new Date());
        cohort.setAvailable(cohortDTO.getAvailable());
        cohort.setDescription(cohortDTO.getDescription());

        CohortEntity cohortEntityResult = cohortRepository.save(cohort);
        resultDTO.setStatus(1);
        resultDTO.setData(cohortEntityResult);
        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> deleteCohorts(List<Long> ids){
        ResultDTO resultDTO = new ResultDTO();
        if(ids == null){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Not choice");
            return ResponseEntity.ok(resultDTO);
        }

        cohortRepository.deleteAllById(ids);
        resultDTO.setStatus(1);
        resultDTO.setMessage("Delete Success");
        return ResponseEntity.ok(resultDTO);
    }




}
