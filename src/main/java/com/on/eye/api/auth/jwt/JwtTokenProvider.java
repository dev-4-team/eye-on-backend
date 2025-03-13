package com.on.eye.api.auth.jwt;

import static com.on.eye.api.auth.constant.AuthConstants.*;
import static com.on.eye.api.global.constants.Constants.MILLI_TO_SECOND;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.on.eye.api.auth.error.exception.ExpiredTokenException;
import com.on.eye.api.auth.error.exception.InvalidTokenException;
import com.on.eye.api.global.common.model.dto.AccessTokenInfo;
import com.on.eye.api.global.common.properties.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        secretKey =
                Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    private Jws<Claims> getJws(String token) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            throw InvalidTokenException.EXCEPTION;
        }
        return claimsJws;
    }

    public String generateAccessToken(Long userId, String role) {
        final Date issuedAt = new Date();
        final Date accessTokenExpiresAt =
                new Date(issuedAt.getTime() + jwtProperties.getExpiration() * MILLI_TO_SECOND);

        return buildAccessToken(userId, issuedAt, accessTokenExpiresAt, role);
    }

    public String generateRefreshToken(Long userId) {
        final Date issuedAt = new Date();
        final Date accessTokenExpiresAt =
                new Date(
                        issuedAt.getTime()
                                + jwtProperties.getRefreshExpiration() * MILLI_TO_SECOND);

        return buildRefreshToken(userId, issuedAt, accessTokenExpiresAt);
    }

    private String buildAccessToken(
            Long userId, Date issuedAt, Date accessTokenExpiresAt, String role) {
        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(issuedAt)
                .subject(userId.toString())
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .claim(TOKEN_ROLE, role)
                .expiration(accessTokenExpiresAt)
                .signWith(secretKey)
                .compact();
    }

    private String buildRefreshToken(Long userId, Date issuedAt, Date refreshTokenExpiresAt) {
        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(issuedAt)
                .subject(userId.toString())
                .claim(TOKEN_TYPE, REFRESH_TOKEN)
                .expiration(refreshTokenExpiresAt)
                .signWith(secretKey)
                .compact();
    }

    public boolean isAccessToken(String token) {
        return getJws(token).getPayload().get(TOKEN_TYPE, String.class).equals(ACCESS_TOKEN);
    }

    public boolean isRefreshToken(String token) {
        return getJws(token).getPayload().get(TOKEN_TYPE, String.class).equals(REFRESH_TOKEN);
    }

    public AccessTokenInfo parseAccessToken(String token) {
        if (!isAccessToken(token)) throw InvalidTokenException.EXCEPTION;

        final Claims claims = getJws(token).getPayload();
        return AccessTokenInfo.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .role(claims.get(TOKEN_ROLE, String.class))
                .build();
    }

    public Long parseRefreshToken(String token) {
        if (!isRefreshToken(token)) throw InvalidTokenException.EXCEPTION;
        Claims claims = getJws(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public Long getAccessTokenTTlSecond() {
        return jwtProperties.getExpiration();
    }

    public Long getRefreshTokenTTlSecond() {
        return jwtProperties.getRefreshExpiration();
    }
}
