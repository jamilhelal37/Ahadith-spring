package com.jamil.ahadith.core.exception;

import com.jamil.ahadith.core.storage.exception.ProfileImageStorageException;
import com.jamil.ahadith.core.storage.exception.ProfileImageValidationException;
import com.jamil.ahadith.core.web.RequestCorrelationFilter;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RawiNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.exception.TopicNotFoundException;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.exception.SimilarAhadithNotFoundException;
import com.jamil.ahadith.features.interaction.exception.CommentNotFoundException;
import com.jamil.ahadith.features.interaction.exception.FavoriteNotFoundException;
import com.jamil.ahadith.features.interaction.exception.QuestionNotFoundException;
import com.jamil.ahadith.features.notification.exception.NotificationNotFoundException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentStorageException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentTooLargeException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeDocumentValidationException;
import com.jamil.ahadith.features.upgrade.exception.UpgradeRequestNotFoundException;
import com.jamil.ahadith.features.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
            UserNotFoundException.class,
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFound(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", safeMessage(ex), request);
    }

    @ExceptionHandler({
            ProfileImageValidationException.class,
            UpgradeDocumentValidationException.class,
            InvalidRequestException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", badRequestMessage(ex), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ErrorResponseDto response = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                request.getRequestURI(),
                LocalDateTime.now(),
                requestId(request),
                fields);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Payload Too Large",
                "Uploaded file exceeds the configured size limit",
                request);
    }

    @ExceptionHandler(UpgradeDocumentTooLargeException.class)
    public ResponseEntity<ErrorResponseDto> handleUpgradeDocumentTooLarge(
            UpgradeDocumentTooLargeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfileImageStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleStorage(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
    }

    @ExceptionHandler(UpgradeDocumentStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleUpgradeDocumentStorage(
            UpgradeDocumentStorageException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Bad Gateway", "Upgrade document storage failed", request);
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailDelivery(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Email delivery failed",
                request);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceUnavailable(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), request);
    }

    @ExceptionHandler({UserAlreadyExistsException.class, ConflictException.class})
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimited(RateLimitException ex, HttpServletRequest request) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        if (ex.getRetryAfter() != null) {
            long retryAfterSeconds = Math.max(1, (ex.getRetryAfter().toMillis() + 999) / 1000);
            response.header("Retry-After", Long.toString(retryAfterSeconds));
        }
        return response.body(errorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                ex.getMessage(),
                request));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponseDto> handleUnauthorized(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "Method not allowed", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported Media Type",
                "Unsupported media type",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", "Database constraint violation", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled request failure requestId={}", requestId(request), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected server error",
                request);
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(errorResponse(status, error, message, request));
    }

    private ErrorResponseDto errorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return new ErrorResponseDto(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        return requestId == null ? null : requestId.toString();
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null ? "Resource not found" : ex.getMessage();
    }

    private String badRequestMessage(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Malformed JSON request";
        }
        if (ex instanceof MissingServletRequestParameterException missing) {
            return "Missing required parameter: " + missing.getParameterName();
        }
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Invalid value for parameter: " + mismatch.getName();
        }
        return safeMessage(ex);
    }
}
