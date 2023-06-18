package com.example.inventoryservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

import java.sql.Timestamp;
import java.time.Instant;

@Data
public class ApiErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Timestamp timestamp;
    private int code;
    private Object reason;
    private String method;
    private String path;

    public ApiErrorResponse(HttpStatusCode status, Object reason, String method, String path) {
        this.timestamp = Timestamp.from(Instant.now());
        this.code = status.value();
        this.reason = reason;
        this.method = method;
        this.path = path;
    }
}
