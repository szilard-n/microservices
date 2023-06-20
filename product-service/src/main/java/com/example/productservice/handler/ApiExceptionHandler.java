package com.example.productservice.handler;

import com.example.productservice.dto.ApiErrorResponse;
import com.example.productservice.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler for all exceptions thrown from the API
 */
@ControllerAdvice
@Order(2)
@Slf4j
public class ApiExceptionHandler {

    private static final String CONTACT_SUPPORT_MSG = "Something went wrong. Please contact support.";

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> resourceNotFoundExceptionHandler(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = {RestClientException.class})
    public ResponseEntity<ApiErrorResponse> restClientException(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiErrorResponse> internalErrorHandler(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(ex.getMessage());
        return buildResponse(request, status, CONTACT_SUPPORT_MSG);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(WebRequest request, HttpStatus status, String message) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpMethod httpMethod = ((ServletWebRequest) request).getHttpMethod();
        ApiErrorResponse apiErrorDto = new ApiErrorResponse(status, message, httpMethod.name(), uri);

        log.error("{} {} -> {}", httpMethod.name(), uri, message);
        return ResponseEntity.status(status).body(apiErrorDto);
    }
}
