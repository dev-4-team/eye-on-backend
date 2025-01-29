package com.on.eye.api.config.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.on.eye.api.helper.SpringEnvHelper;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {
    private final SpringEnvHelper springEnvHelper;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("https://www.eye-on.kr");

        String[] patterns = allowedOrigins.toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(patterns)
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
