package com.example.productservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String ROLE_SELLER = "seller";
    private static final String ROLE_BUYER = "buyer";

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer ->
                        customizer.requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/product").hasRole(ROLE_SELLER)
                                .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole(ROLE_SELLER)
                                .requestMatchers(HttpMethod.POST, "/api/product/purchase").hasAnyRole(ROLE_SELLER, ROLE_BUYER)
                                .requestMatchers(HttpMethod.POST, "/api/product/restock").hasAnyRole(ROLE_SELLER)
                                .anyRequest().authenticated()
                );

        http.oauth2ResourceServer(customizer ->
                customizer.jwt(jwtCustomizer ->
                        jwtCustomizer.jwtAuthenticationConverter(jwtAuthConverter)
                )
        );

        http.sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
