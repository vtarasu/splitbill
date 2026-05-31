package com.example.splitbill.user.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Data
public class ErrorResponse {
    private String errorMessage;
    private int status;
    private LocalDateTime timestamp;
}
