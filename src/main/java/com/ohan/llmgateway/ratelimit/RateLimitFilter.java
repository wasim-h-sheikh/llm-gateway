/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.ratelimit;

import com.ohan.llmgateway.auth.jwt.JwtService;
import com.ohan.llmgateway.common.error.ErrorCode;
import com.ohan.llmgateway.security.AuthTokenResolver;
import com.ohan.llmgateway.security.AuthTokenType;
import com.ohan.llmgateway.security.SecurityErrorResponseWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final JwtService jwtService;
    private final AuthTokenResolver authTokenResolver;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        // IP rate limit
        if (!rateLimitService.isIpAllowed(clientIp)) {
            securityErrorResponseWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    ErrorCode.IP_RATE_LIMIT_EXCEEDED,
                    "IP rate limit exceeded"
            );
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = authHeader.substring(7);
        log.info("Auth token received: {}", token.substring(0, Math.min(10, token.length())));
        AuthTokenType tokenType = authTokenResolver.resolve(token);

        // API key case
        if (tokenType == AuthTokenType.API_KEY) {

            String prefix = token.substring(0, 12);

            if (!rateLimitService.isApiKeyAllowed(prefix)) {
                securityErrorResponseWriter.write(
                        response,
                        HttpStatus.TOO_MANY_REQUESTS,
                        ErrorCode.RATE_LIMIT_EXCEEDED,
                        "API key rate limit exceeded"
                );
                return;
            }
        } else if(tokenType == AuthTokenType.JWT) {

            Long userId = jwtService.extractUserId(token);

            String redisKey = "rate_limit:user:" + userId;

            if (!rateLimitService.isAllowed(redisKey, 60)) {
                securityErrorResponseWriter.write(
                        response,
                        HttpStatus.TOO_MANY_REQUESTS,
                        ErrorCode.RATE_LIMIT_EXCEEDED,
                        "User rate limit exceeded"
                );
                return;
            }
        } else {
            securityErrorResponseWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED_ACCESS,
                    "unauthorized access"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}