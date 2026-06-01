/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmResponse {

    /** Provider value used for resilience fallbacks (not a real provider call). */
    public static final String FALLBACK_PROVIDER = "fallback";

    private String content;

    private int inputTokens;

    private int outputTokens;

    private String provider;

    private String model;

    /** True when this is a resilience fallback rather than a real provider response. */
    public boolean isFallback() {
        return FALLBACK_PROVIDER.equals(provider);
    }
}