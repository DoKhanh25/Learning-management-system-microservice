package com.example.courseservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "geminiClient", url = "https://generativelanguage.googleapis.com")
public interface GeminiClient {
    @PostMapping(value = "/v1beta/models/gemini-2.0-flash:generateContent?key=${gemini.api.key}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    String generateContent(@RequestBody String requestBody);
}
