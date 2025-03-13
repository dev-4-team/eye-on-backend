package com.on.eye.api.global.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.on.eye.api.global.error.exception.SecurityContextNotFoundException;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw SecurityContextNotFoundException.EXCEPTION;

        if (authentication.isAuthenticated()) return Long.valueOf(authentication.getName());

        return 0L;
    }
}
