package com.example.courseservice.feign;

import com.example.courseservice.dto.ResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/getCohortById")
    ResponseEntity<ResultDTO> getCohortById(@RequestParam("id") Long id);

    @GetMapping("/api/getCohortMemberEntitiesByCohortId")
    ResponseEntity<ResultDTO> getCohortMemberEntitiesByCohortId(@RequestParam("id") Long id);
}
