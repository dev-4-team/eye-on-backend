package com.on.eye.api.global.config.security;

import static com.on.eye.api.auth.constant.AuthConstants.BEARER;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.on.eye.api.auth.jwt.JwtTokenProvider;
import com.on.eye.api.global.common.model.dto.AccessTokenInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        // TODO: 단순 null check 말고, JwtTokenProvider에서 validte를 해야 함
        if (token != null) {
            UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(BEARER.length());
        }

        return null;
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        AccessTokenInfo accessTokenInfo = jwtTokenProvider.parseAccessToken(token);

        UserDetails userDetails =
                new AuthDetails(accessTokenInfo.getUserId().toString(), accessTokenInfo.getRole());

        return new UsernamePasswordAuthenticationToken(
                userDetails, "user", userDetails.getAuthorities());
    }
}
