package com.example.productservice.handler;

import com.example.productservice.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;


public class FeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder decoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        // extract reason from client response body
        if (response.status() == HttpStatus.BAD_REQUEST.value()) {
            ApiErrorResponse errorResponse;
            try (InputStream bodyIS = response.body().asInputStream()) {
                ObjectMapper objectMapper = new ObjectMapper();
                errorResponse = objectMapper.readValue(bodyIS, ApiErrorResponse.class);
            } catch (IOException ex) {
                return ex;
            }

            return new RestClientException(errorResponse.getReason().toString());
        }

        return decoder.decode(methodKey, response);
    }
}
