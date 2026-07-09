package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.ErrorResponseDto;
import com.jamil.ahadith.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({
            RulingNotFoundException.class,
            RawiNotFoundException.class,
            HadithNotFoundException.class,
            FakeHadithNotFoundException.class,
            FavoriteNotFoundException.class,
            NotificationNotFoundException.class,
            QuestionNotFoundException.class,
            BookNotFoundException.class,
            ExplainingNotFoundException.class,
            MuhaddithNotFoundException.class,
            TopicNotFoundException.class,
            CommentNotFoundException.class,
            SimilarAhadithNotFoundException.class,
            UpgradeRequestNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFound(RuntimeException ex,
                                                           HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfileImageValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(RuntimeException ex,
                                                             HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                                HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Profile image size must not exceed 2MB", request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponseDto> handleMultipart(MultipartException ex,
                                                            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Request must be multipart/form-data with a file field named 'file'", request);
    }

    @ExceptionHandler(ProfileImageStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleStorage(RuntimeException ex,
                                                          HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex,
                                                           HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler({
            org.springframework.security.authentication.BadCredentialsException.class,
            org.springframework.security.core.userdetails.UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleUnauthorized(RuntimeException ex,
                                                               HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
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
