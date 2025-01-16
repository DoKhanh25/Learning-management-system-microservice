package com.example.userservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class KeycloakException extends RuntimeException{
    private int code;
    private String message;
}
