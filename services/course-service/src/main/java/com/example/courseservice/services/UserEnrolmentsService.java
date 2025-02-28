package com.example.courseservice.services;

import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.UserEnrolmentsEntity;
import com.example.courseservice.repository.UserEnrolmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEnrolmentsService {
    @Autowired
    UserEnrolmentsRepository userEnrolmentsRepository;

    public ResponseEntity<ResultDTO> getAllUserEnrolmentsByCourseId(Long id){
        ResultDTO resultDTO = new ResultDTO();
        List<UserEnrolmentsEntity> userEnrolmentsEntityList = userEnrolmentsRepository.getAllUserEnrolmentsByCourseId(id);
        resultDTO.setStatus(1);
        resultDTO.setData(userEnrolmentsEntityList);
        return ResponseEntity.ok(resultDTO);
    }
}
