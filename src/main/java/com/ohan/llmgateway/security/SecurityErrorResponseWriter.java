/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohan.llmgateway.common.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void write(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {

        // IMPORTANT
        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();

        response.setStatus(status.value());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.setContentType("application/json");

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .requestId(null)
                .build();

        String json = objectMapper.writeValueAsString(error);

        response.getWriter().write(json);

        response.getWriter().flush();
    }
}