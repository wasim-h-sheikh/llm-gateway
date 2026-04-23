/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider.openai;

import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.provider.openai.dto.OpenAiChatRequest;
import com.ohan.llmgateway.provider.openai.dto.OpenAiChatResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiProvider implements LlmProvider {

    private final ProvidersConfig providersConfig;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    @CircuitBreaker(name = "openai", fallbackMethod = "fallback")
    public LlmResponse generate(String model, String prompt) {

        ProviderProperties config = providersConfig.getConfigs().get("openai");

        if (config == null || !config.isEnabled()) {
            throw new RuntimeException("OpenAI provider disabled");
        }

        OpenAiChatRequest request = OpenAiChatRequest.builder()
                .model(model)
                .messages(List.of(
                        OpenAiChatRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        OpenAiChatResponse response = restClient.post()
                .uri(config.getBaseUrl() + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OpenAiChatResponse.class);

        String content = response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        return LlmResponse.builder()
                .content(content)
                .inputTokens(response.getUsage().getPrompt_tokens())
                .outputTokens(response.getUsage().getCompletion_tokens())
                .provider("openai")
                .model(model)
                .build();
    }

    // 🔥 FALLBACK METHOD (MANDATORY SIGNATURE)
    public LlmResponse fallback(String model, String prompt, Throwable t) {

        log.error("OpenAI failed, fallback triggered", t);

        return LlmResponse.builder()
                .content("OpenAI temporarily unavailable. Please try again.")
                .inputTokens(0)
                .outputTokens(0)
                .provider("fallback")
                .model(model)
                .build();
    }
}