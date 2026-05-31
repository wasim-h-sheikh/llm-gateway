/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider.gemini;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.provider.gemini.dto.GeminiChatRequest;
import com.ohan.llmgateway.provider.gemini.dto.GeminiChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Provider backed by the Google Gemini official API.
 *
 * Registered as Spring bean "geminiProvider" (referenced by
 * ModelMetadata.providerBeanName, e.g. for model "gemini-2.5-flash").
 *
 * Uses config key "google" from application.yaml:
 *   base-url: https://generativelanguage.googleapis.com/v1beta/models
 *   api-key:  ${GEMINI_API_KEY}
 *
 * Endpoint shape: POST {baseUrl}/{model}:generateContent?key={apiKey}
 */
@Service("geminiProvider")
@RequiredArgsConstructor
@Slf4j
public class GeminiProvider implements LlmProvider {

    private final ProvidersConfig providersConfig;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public LlmResponse generate(String model, String prompt) {

        ProviderProperties config = providersConfig.getConfigs().get("google");

        if (config == null || !config.isEnabled()) {
            throw new RuntimeException("Gemini provider disabled");
        }

        GeminiChatRequest request = GeminiChatRequest.builder()
                .contents(List.of(
                        GeminiChatRequest.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiChatRequest.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // Gemini authenticates via the ?key= query param, not a bearer header.
        String uri = config.getBaseUrl()
                + "/" + model + ":generateContent?key=" + config.getApiKey();

        log.info("Calling Gemini API. model={}", model);

        GeminiChatResponse response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiChatResponse.class);

        String content = extractContent(response);

        int inputTokens = 0;
        int outputTokens = 0;

        if (response != null && response.getUsageMetadata() != null) {
            inputTokens = response.getUsageMetadata().getPromptTokenCount();
            outputTokens = response.getUsageMetadata().getCandidatesTokenCount();
        }

        return LlmResponse.builder()
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .provider("google")
                .model(model)
                .build();
    }

    private String extractContent(GeminiChatResponse response) {

        if (response == null
                || response.getCandidates() == null
                || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Gemini returned no candidates");
        }

        GeminiChatResponse.Candidate candidate = response.getCandidates().get(0);

        if (candidate.getContent() == null
                || candidate.getContent().getParts() == null
                || candidate.getContent().getParts().isEmpty()) {
            throw new RuntimeException("Gemini returned empty content");
        }

        return candidate.getContent().getParts().stream()
                .map(GeminiChatResponse.Part::getText)
                .filter(text -> text != null && !text.isBlank())
                .reduce("", String::concat);
    }

    // 🔥 FALLBACK METHOD (MANDATORY SIGNATURE)
    public LlmResponse fallback(String model, String prompt, Throwable t) {

        log.error("Gemini failed, fallback triggered", t);

        return LlmResponse.builder()
                .content("Gemini temporarily unavailable. Please try again.")
                .inputTokens(0)
                .outputTokens(0)
                .provider("fallback")
                .model(model)
                .build();
    }
}
