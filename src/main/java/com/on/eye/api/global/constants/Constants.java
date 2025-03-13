package com.on.eye.api.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final int MILLI_TO_SECOND = 1000;
    public static final String ASSET_DOMAIN = "https://asset.eye-on.kr";
    public static final String[] SWAGGER_PATTERNS = {
        "/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs",
    };
}
