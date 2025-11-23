package com.example.backend.cucumber;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@TestConfiguration
public class TestRestTemplateConfig {
    
    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
        // Configure HttpClient with cookie store to maintain session
        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClient httpClient = HttpClientBuilder.create()
            .setDefaultCookieStore(cookieStore)
            .build();
        
        HttpComponentsClientHttpRequestFactory requestFactory = 
            new HttpComponentsClientHttpRequestFactory(httpClient);
        
        return new TestRestTemplate(builder
            .requestFactory(() -> requestFactory));
    }
}
