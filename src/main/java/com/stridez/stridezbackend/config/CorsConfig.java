package com.stridez.stridezbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration bean. This follows the example you provided but uses
 * a pragmatic mapping for APIs and allows headers and OPTIONS requests.
 *
 * Note: For development we allow all origins ("*"). If you need cookies/auth
 * then replace '*' with explicit origin(s) and set allowCredentials(true).
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            // Development-friendly CORS: allow requests from local frontends and mobile devices.
            // - For local testing it's convenient to allow origins/patterns. In production replace
            //   this with a specific list of allowed origins.
            registry.addMapping("/api/**")
                // allow origin patterns so we can accept requests from http://<pc-ip>:<port>, 10.0.2.2, etc.
                .allowedOriginPatterns("http://*", "https://*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                // allow credentials (cookies/auth). allowedOriginPatterns permits this with non-* origins.
                .allowCredentials(true);
            }
        };
    }
}
