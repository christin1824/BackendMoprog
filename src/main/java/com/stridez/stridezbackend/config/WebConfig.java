package com.stridez.stridezbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;

/**
 * Global CORS configuration for development.
 *
 * Note: For development it's convenient to allow all origins so Flutter web (served
 * on dynamic ports) can call the API. In production replace the wildcard with the
 * explicit frontend origin(s) and set allowCredentials(true) if using cookies.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Development: allow any origin so mobile/web dev tools can call the API.
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
