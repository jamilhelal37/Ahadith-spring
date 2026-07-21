package com.jamil.ahadith.core.security;

import com.jamil.ahadith.core.exception.ErrorResponseDto;
import com.jamil.ahadith.core.web.RequestCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityExceptionResponder {
    private final ObjectMapper objectMapper;

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String error,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorResponseDto(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                requestId(request)));
    }

    private String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        return requestId == null ? null : requestId.toString();
    }
}
