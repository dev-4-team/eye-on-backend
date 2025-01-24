package com.on.eye.api.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "auth.jwt")
// Immutable한 property 사용.
public class JwtProperties {
    private final String secretKey;
    private final Long expiration;
    private final Long refreshExpiration;
}
