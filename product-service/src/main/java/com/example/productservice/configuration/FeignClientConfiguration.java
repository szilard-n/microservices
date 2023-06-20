package com.example.productservice.configuration;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                requestTemplate.header(HttpHeaders.AUTHORIZATION,"Bearer " + jwt.getTokenValue());
            }
        };
    }

}
