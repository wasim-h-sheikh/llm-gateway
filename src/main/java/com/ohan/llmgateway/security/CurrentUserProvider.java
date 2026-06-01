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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the {@link AuthenticatedPrincipal} from the current {@code SecurityContext}.
 *
 * <p>Backed by a thread-local context, so this must be called on the request
 * thread (e.g. in a controller or synchronously at the top of a service / Flux
 * assembly), not from a deferred reactive callback running on another thread.
 */
@Component
public class CurrentUserProvider {

    public AuthenticatedPrincipal currentPrincipal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            return principal;
        }
        return null;
    }

    public Long currentUserId() {
        AuthenticatedPrincipal principal = currentPrincipal();
        return principal == null ? null : principal.getUserId();
    }

    public Long currentApiKeyId() {
        AuthenticatedPrincipal principal = currentPrincipal();
        return principal == null ? null : principal.getApiKeyId();
    }
}
