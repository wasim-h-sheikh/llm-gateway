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

package com.ohan.llmgateway.provider.registry;

import com.ohan.llmgateway.provider.LlmProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProviderRegistry {

    private final Map<String, LlmProvider> providers;

    public ProviderRegistry(Map<String, LlmProvider> providers) {
        this.providers = providers;
    }

    public LlmProvider getProvider(String name) {
        LlmProvider provider = providers.get(name);
        if (provider == null) {
            throw new RuntimeException("Provider not found: " + name);
        }
        return provider;
    }

    public Map<String, LlmProvider> getAllProviders() {
        return providers;
    }
}