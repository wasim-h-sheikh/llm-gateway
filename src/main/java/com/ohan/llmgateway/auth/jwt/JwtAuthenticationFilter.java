/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.auth.jwt;

import com.ohan.llmgateway.auth.entity.User;
import com.ohan.llmgateway.auth.repository.UserRepository;
import com.ohan.llmgateway.common.error.ErrorCode;
import com.ohan.llmgateway.security.AuthTokenResolver;
import com.ohan.llmgateway.security.AuthTokenType;
import com.ohan.llmgateway.security.AuthenticatedPrincipal;
import com.ohan.llmgateway.security.SecurityErrorResponseWriter;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuthTokenResolver authTokenResolver;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info("Auth token received: {}", token.substring(0, Math.min(10, token.length())));

        // Skip API keys
        AuthTokenType tokenType = authTokenResolver.resolve(token);

        if (tokenType != AuthTokenType.JWT) {
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        try {

            username = jwtService.extractUsername(token);

        } catch (JwtException e) {

            securityErrorResponseWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.INVALID_JWT,
                    "Invalid JWT token"
            );

            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var userDetails = userDetailsService.loadUserByUsername(username);

            Long userId = userRepository.findByEmail(username)
                    .map(User::getId)
                    .orElse(null);

            var principal = AuthenticatedPrincipal.builder()
                    .userId(userId)
                    .email(username)
                    .apiKeyId(null)
                    .build();

            var authToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    userDetails.getAuthorities()
            );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}