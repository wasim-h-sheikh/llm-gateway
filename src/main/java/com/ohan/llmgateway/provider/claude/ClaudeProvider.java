/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider.claude;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.provider.claude.dto.ClaudeChatRequest;
import com.ohan.llmgateway.provider.claude.dto.ClaudeChatResponse;
import com.ohan.llmgateway.provider.dto.LlmResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Provider backed by the Anthropic (Claude) official Messages API.
 *
 * Registered as Spring bean "claudeProvider" (referenced by
 * ModelMetadata.providerBeanName, e.g. for model "claude-haiku-4-5").
 *
 * Uses config key "claude" from application.yaml:
 *   base-url: https://api.anthropic.com/v1/messages   (full endpoint)
 *   api-key:  ${ANTHROPIC_API_KEY}
 *
 * Auth is via the "x-api-key" header plus a required "anthropic-version" header.
 */
@Service("claudeProvider")
@RequiredArgsConstructor
@Slf4j
public class ClaudeProvider implements LlmProvider {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private static final int DEFAULT_MAX_TOKENS = 4096;

    private final ProvidersConfig providersConfig;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public LlmResponse generate(String model, String prompt) {

        ProviderProperties config = providersConfig.getConfigs().get("claude");

        if (config == null || !config.isEnabled()) {
            throw new RuntimeException("Claude provider disabled");
        }

        ClaudeChatRequest request = ClaudeChatRequest.builder()
                .model(model)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .messages(List.of(
                        ClaudeChatRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        log.info("Calling Claude API. model={}", model);

        // base-url is the full Messages endpoint, so no path is appended.
        ClaudeChatResponse response = restClient.post()
                .uri(config.getBaseUrl())
                .header("x-api-key", config.getApiKey())
                .header("anthropic-version", ANTHROPIC_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ClaudeChatResponse.class);

        String content = extractContent(response);

        int inputTokens = 0;
        int outputTokens = 0;

        if (response.getUsage() != null) {
            inputTokens = response.getUsage().getInputTokens();
            outputTokens = response.getUsage().getOutputTokens();
        }

        return LlmResponse.builder()
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .provider("claude")
                .model(model)
                .build();
    }

    private String extractContent(ClaudeChatResponse response) {

        if (response == null
                || response.getContent() == null
                || response.getContent().isEmpty()) {
            throw new RuntimeException("Claude returned no content");
        }

        // Concatenate all "text" blocks; non-text blocks are ignored.
        String text = response.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(ClaudeChatResponse.ContentBlock::getText)
                .filter(value -> value != null && !value.isBlank())
                .reduce("", String::concat);

        if (text.isBlank()) {
            throw new RuntimeException("Claude returned empty text content");
        }

        return text;
    }

    // 🔥 FALLBACK METHOD (MANDATORY SIGNATURE)
    public LlmResponse fallback(String model, String prompt, Throwable t) {

        log.error("Claude failed, fallback triggered", t);

        return LlmResponse.builder()
                .content("Claude temporarily unavailable. Please try again.")
                .inputTokens(0)
                .outputTokens(0)
                .provider(LlmResponse.FALLBACK_PROVIDER)
                .model(model)
                .build();
    }
}
