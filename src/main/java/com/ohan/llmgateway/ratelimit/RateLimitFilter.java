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
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        // IP rate limit
        if (!rateLimitService.isIpAllowed(clientIp)) {
            response.setStatus(429);
            response.getWriter().write("IP rate limit exceeded");
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            // API key case
            if (token.startsWith("sk_")) {

                String prefix = token.substring(0, 12);

                if (!rateLimitService.isApiKeyAllowed(prefix)) {
                    response.setStatus(429);
                    response.getWriter().write("API key rate limit exceeded");
                    return;
                }
            }

            // JWT case
            else {

                Long userId = jwtService.extractUserId(token);

                String redisKey = "rate_limit:user:" + userId;

                if (!rateLimitService.isAllowed(redisKey, 60)) {
                    response.setStatus(429);
                    response.getWriter().write("User rate limit exceeded");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}