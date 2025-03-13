package com.on.eye.api.global.helper;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.on.eye.api.global.common.annotation.Helper;

import lombok.RequiredArgsConstructor;

@Helper
@RequiredArgsConstructor
public class SpringEnvHelper {
    private final Environment environment;

    private static final String PROD = "prod";
    private static final String STAGING = "staging";
    private static final String DEV = "dev";

    private static final List<String> PROD_AND_STAGING = List.of(PROD, STAGING);

    public Boolean isProdProfile() {
        return getCurrentProfile().contains(PROD);
    }

    public Boolean isStagingProfile() {
        return getCurrentProfile().contains(STAGING);
    }

    public Boolean isDevProfile() {
        return getCurrentProfile().contains(DEV);
    }

    public Boolean isProdOrStagingProfile() {
        return CollectionUtils.containsAny(PROD_AND_STAGING, getCurrentProfile());
    }

    private List<String> getCurrentProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.stream(activeProfiles).toList();
    }
}
