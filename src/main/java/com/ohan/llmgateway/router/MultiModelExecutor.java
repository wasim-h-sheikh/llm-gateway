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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class MultiModelExecutor {

    private final ProviderRegistry providerRegistry;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public Map<String, LlmResponse> executeParallel(
            List<String> providers,
            String model,
            String prompt
    ) {

        Map<String, Future<LlmResponse>> futures = new HashMap<>();

        for (String providerName : providers) {

            LlmProvider provider = providerRegistry.getProvider(providerName);

            Future<LlmResponse> future = executor.submit(() ->
                    provider.generate(model, prompt)
            );

            futures.put(providerName, future);
        }

        Map<String, LlmResponse> results = new HashMap<>();

        for (Map.Entry<String, Future<LlmResponse>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                results.put(entry.getKey(), null);
            }
        }

        return results;
    }
}