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

package com.ohan.llmgateway.provider.nvidia;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.provider.dto.LlmResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NvidiaProvider implements LlmProvider {

    private final ProvidersConfig providersConfig;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public LlmResponse generate(String model, String prompt) {

        ProviderProperties config = providersConfig.getConfigs().get("nvidia");
        log.info("NVIDIA API key present: {}", config.getApiKey() != null);

        log.info("NVIDIA API key starts with: {}",
                config.getApiKey() != null ? config.getApiKey().substring(0, 5) : "null");
        if (config == null || !config.isEnabled()) {
            throw new RuntimeException("NVIDIA provider disabled");
        }

        try {

            Map<String, Object> request = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7
            );
            log.info("Calling NVIDIA API...");
            Map response = restClient.post()
                    .uri(config.getBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            log.info("NVIDIA API responded");
            // 🔥 Extract response safely
            Map choice = (Map) ((List) response.get("choices")).get(0);
            Map message = (Map) choice.get("message");

            String content = (String) message.get("content");

            Map usage = (Map) response.get("usage");

            int inputTokens = (int) usage.get("prompt_tokens");
            int outputTokens = (int) usage.get("completion_tokens");

            return LlmResponse.builder()
                    .content(content)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .provider("nvidia")
                    .model(model)
                    .build();

        } catch (Exception e) {
            log.error("NVIDIA API failed", e);
            throw new RuntimeException("NVIDIA provider error", e);
        }
    }
}