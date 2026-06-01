/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.execution.service;

import com.ohan.llmgateway.cache.PromptCacheService;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.ModelRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Central execution path for all non-streaming chat (completions, compare,
 * intelligent routing). Prompt caching lives here so every caller benefits
 * without duplicating cache logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmExecutionService {

    private final ModelRouter modelRouter;
    private final PromptCacheService promptCacheService;

    public LlmResponse execute(
            String model,
            String prompt
    ) {

        String cacheKey = promptCacheService.completionKey(model, prompt);

        // Check the central prompt cache before hitting the provider
        LlmResponse cached = promptCacheService.get(cacheKey, LlmResponse.class);
        if (cached != null) {
            log.info(
                    "Prompt cache HIT - returning cached response. model={} provider={}",
                    cached.getModel(),
                    cached.getProvider()
            );
            return cached;
        }

        log.info(
                "Prompt cache MISS - executing model request. model={}",
                model
        );

        LlmResponse response = modelRouter.route(
                model,
                prompt
        );

        // Store the full response (content + provider/token metadata) for reuse
        promptCacheService.put(cacheKey, response);
        log.info(
                "Prompt cache STORE - cached response. model={}",
                response.getModel()
        );

        return response;
    }
}
