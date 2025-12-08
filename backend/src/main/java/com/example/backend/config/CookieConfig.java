package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;

@Configuration
public class CookieConfig {

    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone().whenHasName("JSESSIONID");
    }
}
