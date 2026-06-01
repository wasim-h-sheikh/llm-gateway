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
import com.ohan.llmgateway.security.CurrentUserProvider;
import com.ohan.llmgateway.usage.dto.UsageContext;
import com.ohan.llmgateway.usage.service.UsageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Central execution path for all non-streaming chat (completions, compare,
 * intelligent routing). Prompt caching and usage tracking live here so every
 * caller benefits without duplicating the logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmExecutionService {

    private final ModelRouter modelRouter;
    private final PromptCacheService promptCacheService;
    private final UsageService usageService;
    private final CurrentUserProvider currentUserProvider;

    public LlmResponse execute(
            String model,
            String prompt
    ) {

        String cacheKey = promptCacheService.completionKey(model, prompt);

        LlmResponse response;

        // Check the central prompt cache before hitting the provider
        LlmResponse cached = promptCacheService.get(cacheKey, LlmResponse.class);
        if (cached != null) {
            log.info(
                    "Prompt cache HIT - returning cached response. model={} provider={}",
                    cached.getModel(),
                    cached.getProvider()
            );
            response = cached;
        } else {
            log.info(
                    "Prompt cache MISS - executing model request. model={}",
                    model
            );

            response = modelRouter.route(
                    model,
                    prompt
            );

            // Store the full response (content + provider/token metadata) for reuse
            promptCacheService.put(cacheKey, response);
            log.info(
                    "Prompt cache STORE - cached response. model={}",
                    response.getModel()
            );
        }

        // Central usage tracking for every non-streaming chat path
        // (recorded for both fresh and cached responses). Identity is read here
        // on the request thread, where the SecurityContext is available.
        usageService.recordUsage(
                UsageContext.builder()
                        .userId(currentUserProvider.currentUserId())
                        .apiKeyId(currentUserProvider.currentApiKeyId())
                        .provider(response.getProvider())
                        .model(response.getModel())
                        .inputTokens(response.getInputTokens())
                        .outputTokens(response.getOutputTokens())
                        .build()
        );
        log.info(
                "Usage recorded. model={} provider={} inputTokens={} outputTokens={}",
                response.getModel(),
                response.getProvider(),
                response.getInputTokens(),
                response.getOutputTokens()
        );

        return response;
    }
}
