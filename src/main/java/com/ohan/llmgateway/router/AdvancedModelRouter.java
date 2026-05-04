/*
 *
 *  *
 *  *  * Copyright (c) 2026 Wasim Sheikh
 *  *  * Project: LLM Gateway
 *  *  *
 *  *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  *  * Proprietary and confidential.
 *  *
 *
 *
 */

package com.ohan.llmgateway.router;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.provider.registry.ProviderRegistry;
import com.ohan.llmgateway.resilience.ResilientProviderExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedModelRouter {

    private final ProviderRegistry providerRegistry;
    private final ResilientProviderExecutor executor;

    public LlmResponse route(String model, String prompt) {

        List<String> providers = resolveProviders(model);

        Exception lastException = null;

        for (String providerName : providers) {
            try {

                LlmProvider provider = providerRegistry.getProvider(providerName);

                return executor.execute(
                        providerName,
                        provider,
                        model,
                        prompt
                );

            } catch (Exception e) {
                log.error("Provider failed: {}", providerName, e);
                lastException = e;
            }
        }

        throw new RuntimeException("All providers failed", lastException);
    }

    private List<String> resolveProviders(String model) {

        // 🔥 NVIDIA models (your 20 models)

        if (model.contains("/")) {
            return List.of("nvidiaProvider");
        }

        if (model.startsWith("gpt")) {
            return List.of("openAiProvider");
        }

        return List.of("openAiProvider");
    }
}