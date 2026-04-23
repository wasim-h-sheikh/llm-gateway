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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedModelRouter {

    private final ProviderRegistry providerRegistry;

    public LlmResponse route(String model, String prompt) {

        // Routing Strategy (can be DB/config driven later)
        List<String> providers = resolveProviders(model);

        Exception lastException = null;

        for (String providerName : providers) {
            try {
                log.info("Trying provider: {}", providerName);

                LlmProvider provider = providerRegistry.getProvider(providerName);

                return provider.generate(model, prompt);

            } catch (Exception e) {
                log.error("Provider failed: {}", providerName, e);
                lastException = e;
            }
        }

        throw new RuntimeException("All providers failed", lastException);
    }

    private List<String> resolveProviders(String model) {

        if (model.startsWith("gpt")) {
            return List.of("openAiProvider"); // fallback chain later
        }

        if (model.startsWith("claude")) {
            return List.of("anthropicProvider", "openAiProvider");
        }

        return List.of("openAiProvider");
    }
}