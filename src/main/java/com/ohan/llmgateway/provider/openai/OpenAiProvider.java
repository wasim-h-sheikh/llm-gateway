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
    public LlmResponse generate(String model, String prompt) {

        ProviderProperties config = providersConfig.getConfigs().get("openai");
        if (config == null || !config.isEnabled()) {
            throw new RuntimeException("OpenAI provider disabled");
        }

        try {

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

            int inputTokens = response.getUsage().getPrompt_tokens();
            int outputTokens = response.getUsage().getCompletion_tokens();

            return LlmResponse.builder()
                    .content(content)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .provider("openai")
                    .model(model)
                    .build();

        } catch (Exception e) {

            log.error("OpenAI API call failed", e);

            throw new RuntimeException("OpenAI provider error", e);
        }
    }
}