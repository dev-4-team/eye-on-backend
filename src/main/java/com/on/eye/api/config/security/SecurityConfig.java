package com.on.eye.api.config.security;

import static com.on.eye.api.constants.AuthConstants.ALLOW_URLS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.on.eye.api.auth.service.CustomOAuth2UserService;
import com.on.eye.api.auth.service.OAuth2AuthenticationFailureHandler;
import com.on.eye.api.auth.service.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtTokenFilter jwtTokenFilter;
    private final AccessDeniedFilter accessDeniedFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // TODO: Swagger용 인증 우회 설정 가능

        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        sessionManagementConfig ->
                                sessionManagementConfig.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        request -> {
                            request.requestMatchers(ALLOW_URLS).permitAll();
                            request.anyRequest().permitAll();
                            //
                            // request.requestMatchers("/api/protest/enlist").authenticated();
                        })
                .oauth2Login(
                        configurer ->
                                configurer
                                        .authorizationEndpoint(
                                                authorizationEndpoint ->
                                                        authorizationEndpoint.baseUri(
                                                                "/oauth2/authorization"))
                                        .userInfoEndpoint(
                                                endpoint ->
                                                        endpoint.userService(
                                                                customOAuth2UserService))
                                        .successHandler(oAuth2AuthenticationSuccessHandler)
                                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jwtExceptionFilter, JwtTokenFilter.class)
                .addFilterBefore(accessDeniedFilter, AuthorizationFilter.class)
                .build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }

    @Bean
    public DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler =
                new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }
}
