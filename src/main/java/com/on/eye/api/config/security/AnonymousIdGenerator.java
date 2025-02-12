package com.on.eye.api.config.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.on.eye.api.exception.HashNotGeneratedException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AnonymousIdGenerator {
    private static final String HAMAC_SHA256 = "HmacSHA256";

    @Value("${hash.secret-key}")
    private String secretKey;

    public String generateAnonymousUserId(Long userId, Long protestId) {
        try {
            SecretKeySpec keySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HAMAC_SHA256);

            Mac mac = Mac.getInstance(HAMAC_SHA256);
            mac.init(keySpec);

            String message = String.format("%d:%d", protestId, userId);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw HashNotGeneratedException.EXCEPTION;
        }
    }
}
