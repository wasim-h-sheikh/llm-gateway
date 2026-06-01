/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.resilience;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.dto.LlmResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResilientProviderExecutor {

    @CircuitBreaker(name = "llmProvider", fallbackMethod = "fallback")
    public LlmResponse execute(
            String providerName,
            LlmProvider provider,
            String model,
            String prompt
    ) {

        log.info("Executing provider: {}", providerName);

        return provider.generate(model, prompt);
    }

    // 🔥 GLOBAL FALLBACK
    public LlmResponse fallback(
            String providerName,
            LlmProvider provider,
            String model,
            String prompt,
            Throwable t
    ) {

        log.error("Provider {} failed. Triggering fallback", providerName, t);

        return LlmResponse.builder()
                .content("Provider temporarily unavailable")
                .inputTokens(0)
                .outputTokens(0)
                .provider(LlmResponse.FALLBACK_PROVIDER)
                .model(model)
                .build();
    }
}