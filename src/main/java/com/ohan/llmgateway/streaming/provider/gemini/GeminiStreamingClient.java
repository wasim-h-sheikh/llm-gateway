/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.provider.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.provider.StreamingProviderClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiStreamingClient implements StreamingProviderClient {

    private final WebClient webClient;
    private final ProvidersConfig providersConfig;

    @Override
    public boolean supports(String provider) {
        return "google".equalsIgnoreCase(provider);
    }

    @Override
    public Flux<StreamingChunk> stream(
            StreamingChatRequest request
    ) {

        ProviderProperties config =
                providersConfig.getConfigs()
                        .get("google");

        if (config == null || !config.isEnabled()) {
            return Flux.error(
                    new RuntimeException(
                            "Gemini provider disabled"
                    )
            );
        }

        Map<String, Object> body = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                request.getPrompt()
                                        )
                                )
                        )
                )
        );

        String url =
                config.getBaseUrl()
                        + "/"
                        + request.getModel()
                        + ":streamGenerateContent?alt=sse&key="
                        + config.getApiKey();

        log.info(
                "Starting Gemini stream. Model={}",
                request.getModel()
        );

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .flatMap(node ->
                        parseChunk(node, request)
                )
                .doOnCancel(() ->
                        log.info("Gemini stream cancelled")
                )
                .doOnComplete(() ->
                        log.info("Gemini stream completed")
                )
                .onErrorResume(ex -> {

                    log.error(
                            "Gemini streaming failed",
                            ex
                    );

                    return Flux.just(
                            StreamingChunk.builder()
                                    .provider("google")
                                    .model(request.getModel())
                                    .error(true)
                                    .completed(true)
                                    .errorMessage(
                                            ex.getMessage()
                                    )
                                    .build()
                    );
                });
    }

    private Flux<StreamingChunk> parseChunk(
            JsonNode root,
            StreamingChatRequest request
    ) {

        try {

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {
                return Flux.empty();
            }

            JsonNode candidate =
                    candidates.get(0);

            /*
             * Gemini final chunk:
             *
             * "finishReason":"STOP"
             */
            String finishReason =
                    candidate.path("finishReason")
                            .asText(null);

            if (finishReason != null) {

                return Flux.just(
                        StreamingChunk.builder()
                                .provider("google")
                                .model(request.getModel())
                                .completed(true)
                                .build()
                );
            }

            JsonNode parts =
                    candidate.path("content")
                            .path("parts");

            if (!parts.isArray()
                    || parts.isEmpty()) {
                return Flux.empty();
            }

            String content =
                    parts.get(0)
                            .path("text")
                            .asText("");

            if (content.isBlank()) {
                return Flux.empty();
            }

            return Flux.just(
                    StreamingChunk.builder()
                            .provider("google")
                            .model(request.getModel())
                            .content(content)
                            .completed(false)
                            .build()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse Gemini chunk",
                    ex
            );

            return Flux.empty();
        }
    }
}