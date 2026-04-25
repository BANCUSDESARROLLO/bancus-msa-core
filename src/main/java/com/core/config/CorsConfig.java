package com.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuracion CORS para el servicio core.
 */
@Configuration
public class CorsConfig {

    @Value("${core.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,https://*.bancus.com,https://*.bancup.com,https://bancus-dev.di0cd8gp9bhtr.amplifyapp.com,https://bancus-prod.di0cd8gp9bhtr.amplifyapp.com,https://bancus-integration.di0cd8gp9bhtr.amplifyapp.com}")
    private List<String> allowedOriginPatterns;

    @Value("${core.cors.allowed-methods:*}")
    private List<String> allowedMethods;

    @Value("${core.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${core.cors.exposed-headers:*}")
    private List<String> exposedHeaders;

    @Value("${core.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${core.cors.max-age-seconds:3600}")
    private long maxAgeSeconds;

    @Bean
    public WebMvcConfigurer webMvcCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new))
                        .allowedMethods(allowedMethods.toArray(String[]::new))
                        .allowedHeaders(allowedHeaders.toArray(String[]::new))
                        .exposedHeaders(exposedHeaders.toArray(String[]::new))
                        .allowCredentials(allowCredentials)
                        .maxAge(maxAgeSeconds);
            }
        };
    }
}
