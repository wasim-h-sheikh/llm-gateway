/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.config;

import com.ohan.llmgateway.common.dto.ApiErrorResponse;
import com.ohan.llmgateway.common.error.ErrorCode;
import com.ohan.llmgateway.common.exception.InvalidApiKeyException;
import com.ohan.llmgateway.common.exception.ProviderException;
import com.ohan.llmgateway.common.exception.RateLimitExceededException;

import io.jsonwebtoken.JwtException;

import org.slf4j.MDC;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex
    ) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_CREDENTIALS,
                "Invalid email or password"
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwt(
            JwtException ex
    ) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "INVALID_JWT",
                "Invalid or expired JWT token"
        );
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleApiKey(
            InvalidApiKeyException ex
    ) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_API_KEY,
                ex.getMessage()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(
            RateLimitExceededException ex
    ) {

        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleProvider(
            ProviderException ex
    ) {

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.PROVIDER_ERROR,
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex
    ) {

        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Something went wrong"
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message
    ) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(status).body(response);
    }
}