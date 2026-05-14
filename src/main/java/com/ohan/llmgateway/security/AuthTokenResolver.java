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

import org.springframework.stereotype.Component;

@Component
public class AuthTokenResolver {

    public AuthTokenType resolve(String token) {

        if (token == null || token.isBlank()) {
            return AuthTokenType.UNKNOWN;
        }

        // API key
        if (token.startsWith("sk_")) {
            return AuthTokenType.API_KEY;
        }

        // JWT format check
        if (token.chars().filter(ch -> ch == '.').count() == 2) {
            return AuthTokenType.JWT;
        }

        return AuthTokenType.UNKNOWN;
    }
}