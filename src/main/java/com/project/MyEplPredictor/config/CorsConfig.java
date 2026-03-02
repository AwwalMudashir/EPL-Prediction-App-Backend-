package com.project.MyEplPredictor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://127.0.0.1:5173","http://localhost:5173","http://127.0.0.1:5174","http://localhost:5174","https://epl-predictor.netlify.app/","https://epl-predictor-frontend-prod.vercel.app/") // Frontend origin
                        .allowedMethods("*") // Allow all HTTP methods
                        .allowedHeaders("*" ) // Allow all headers
                        .allowCredentials(true);
            }
        };
    }
}
