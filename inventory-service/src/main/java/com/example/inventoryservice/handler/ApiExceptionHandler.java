package com.example.inventoryservice.handler;

import com.example.inventoryservice.dto.ApiErrorResponse;
import com.example.inventoryservice.exception.InsufficientQuantityException;
import com.example.inventoryservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Handler for all exceptions thrown from the API
 */
@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<ApiErrorResponse> NotFoundExceptionHandler(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = {InsufficientQuantityException.class})
    public ResponseEntity<ApiErrorResponse> insufficientQuantityExceptionHandler(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(request, status, ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(WebRequest request, HttpStatus status, String message) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpMethod httpMethod = ((ServletWebRequest) request).getHttpMethod();
        ApiErrorResponse apiErrorDto = new ApiErrorResponse(status, message, httpMethod.name(), uri);

        log.error("{} {} -> {}", httpMethod.name(), uri, message);
        return ResponseEntity.status(status).body(apiErrorDto);
    }
}
