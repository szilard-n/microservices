package com.example.productservice.configuration;

import com.example.productservice.dto.KeycloakTokenResponse;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Slf4j
public class FeignClientAuthConfiguration {

    @Value("${spring.security.oauth2.client.registration.product-service.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.product-service.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak-provider.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.product-service.authorization-grant-type}")
    private String grantType;

    @Bean
    public RequestInterceptor requestInterceptor() {
        MultiValueMap<String, String> requestBodyParameters = new LinkedMultiValueMap<>();
        requestBodyParameters.add("client_id", clientId);
        requestBodyParameters.add("client_secret", clientSecret);
        requestBodyParameters.add("grant_type", grantType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestBody = new HttpEntity<>(requestBodyParameters, headers);

        RestTemplate restTemplate = new RestTemplate();

        return request -> {
            ResponseEntity<KeycloakTokenResponse> keycloakResponse = restTemplate.postForEntity(tokenUri, requestBody, KeycloakTokenResponse.class);

            String accessToken = "";
            if (keycloakResponse.getStatusCode().is2xxSuccessful() && keycloakResponse.getBody() != null) {
                accessToken = keycloakResponse.getBody().accessToken();
            } else {
                log.error("Keycloak responded with {} when fetching the access token", keycloakResponse.getStatusCode());
            }

            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        };
    }
}
