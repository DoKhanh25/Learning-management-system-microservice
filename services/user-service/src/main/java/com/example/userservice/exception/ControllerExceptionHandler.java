package com.example.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<String> handleKeycloakException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(401).body("keycloak create user error");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnwantedException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(500).body("internal error");
    }
}
