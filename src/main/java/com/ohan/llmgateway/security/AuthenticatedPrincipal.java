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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Authenticated identity stored as the principal in the {@code SecurityContext}.
 * Carries the database ids needed for usage attribution. {@code apiKeyId} is
 * {@code null} for JWT-authenticated requests.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedPrincipal implements Serializable {

    private final Long userId;

    private final String email;

    private final Long apiKeyId;

    /**
     * Returns the email. Spring's {@code Authentication.getName()} falls back to
     * {@code principal.toString()} for a non-UserDetails principal, so existing
     * callers that use {@code auth.getName()} to read the email keep working.
     */
    @Override
    public String toString() {
        return email;
    }
}
