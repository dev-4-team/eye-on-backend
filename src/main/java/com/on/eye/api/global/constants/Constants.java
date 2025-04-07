package com.on.eye.api.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final int MILLI_TO_SECOND = 1000;
    public static final String ASSET_DOMAIN = "https://asset.eye-on.kr";
    public static final String[] SWAGGER_PATTERNS = {
        "/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs",
    };
    public static final String[] CORS_ALLOW_LIST = {
        "http://localhost:3000",
        "http://localhost:63342",
        "https://www.eye-on.kr",
        "https://eye-on.kr",
        "https://smartcow-test.ddnsking.com",
    };
}
