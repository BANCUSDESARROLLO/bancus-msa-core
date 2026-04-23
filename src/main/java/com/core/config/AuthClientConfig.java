package com.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AuthClientConfig {

    @Bean
    public RestClient authRestClient(
            @Value("${clients.auth.base-url:http://localhost:8080}") String authBaseUrl,
            @Value("${clients.auth.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${clients.auth.read-timeout-ms:3000}") int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(authBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
