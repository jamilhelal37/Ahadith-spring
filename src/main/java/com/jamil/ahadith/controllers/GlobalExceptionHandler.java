package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.ErrorResponseDto;
import com.jamil.ahadith.exceptions.RawiNotFoundException;
import com.jamil.ahadith.exceptions.RulingNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RulingNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRulingNotFound(RulingNotFoundException ex,
                                                                   HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(RawiNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRawiNotFound(RawiNotFoundException ex,
                                                                 HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String error, String message,
                                                                HttpServletRequest request) {
        ErrorResponseDto response = new ErrorResponseDto(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }



}
