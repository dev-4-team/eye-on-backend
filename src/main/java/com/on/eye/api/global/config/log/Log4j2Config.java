package com.on.eye.api.global.config.log;

import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Log4j2Config {
    @Bean
    public LoggingSystem loggingSystem() {
        return LoggingSystem.get(this.getClass().getClassLoader());
    }
}
