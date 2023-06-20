package com.example.productservice.configuration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring actuator config class
 */
@Configuration
public class ApiLogConfiguration {

    /**
     * Log each request and response
     */
    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new InMemoryHttpExchangeRepository() {
            final Logger logger = LoggerFactory.getLogger(InMemoryHttpExchangeRepository.class);

            @Override
            public void add(HttpExchange httpExchange) {
                logger.info(buildLog(httpExchange));
                super.add(httpExchange);
            }
        };
    }

    private String buildLog(HttpExchange exchange) {
        return exchange.getRequest().getMethod() + " " +
                exchange.getRequest().getUri() + " -> " +
                exchange.getResponse().getStatus();
    }
}
