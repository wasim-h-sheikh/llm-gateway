/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.provider.openai;

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
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiStreamingClient
        implements StreamingProviderClient {

    private final WebClient webClient;

    private final ProvidersConfig providersConfig;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "openai".equalsIgnoreCase(provider);
    }

    @Override
    public Flux<StreamingChunk> stream(
            StreamingChatRequest request
    ) {

        ProviderProperties config =
                providersConfig.getConfigs()
                        .get("openai");

        if (config == null || !config.isEnabled()) {

            return Flux.error(
                    new RuntimeException(
                            "OpenAI provider disabled"
                    )
            );
        }

        Map<String, Object> body = Map.of(
                "model", request.getModel(),
                "stream", true,
                "messages", new Object[]{
                        Map.of(
                                "role", "user",
                                "content",
                                request.getPrompt()
                        )
                }
        );

        log.info(
                "Starting OpenAI stream request for model: {}",
                request.getModel()
        );

        return webClient.post()

                .uri(config.getBaseUrl()
                                + "/chat/completions"
                )
                .header("Authorization",
                        "Bearer "
                                + config.getApiKey()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(60))
                .flatMap(chunk ->
                        parseChunk(chunk, request)
                )
                .doOnCancel(() ->
                        log.info(
                                "OPENAI stream cancelled"
                        )
                )
                .doOnComplete(() ->
                        log.info(
                                "OPENAI stream completed"
                        )
                )
                .onErrorResume(ex -> {
                    log.error(
                            "Streaming error",
                            ex
                    );
                    return Flux.just(
                            StreamingChunk.builder()
                                    .provider("openai")
                                    .model(
                                            request.getModel()
                                    )
                                    .error(true)
                                    .errorMessage(
                                            ex.getMessage()
                                                    != null
                                                    ? ex.getMessage()
                                                    : "Unknown streaming error"
                                    )
                                    .completed(true)
                                    .build()
                    );
                });
    }

    private Flux<StreamingChunk> parseChunk(
            String rawChunk,
            StreamingChatRequest request
    ) {

        try {

            if (rawChunk == null
                    || rawChunk.isBlank()) {

                return Flux.empty();
            }

            rawChunk = rawChunk.trim();

            log.info(
                    "Parsing OPENAI chunk: {}",
                    rawChunk
            );

            /*
             * Handle SSE format:
             * data: {...}
             */
            if (rawChunk.startsWith("data:")) {

                rawChunk = rawChunk
                        .substring(5)
                        .trim();
            }

            /*
             * Stream completed
             */
            if (rawChunk.equals("[DONE]")) {

                return Flux.just(
                        StreamingChunk.builder()
                                .provider("openai")
                                .model(request.getModel())
                                .completed(true)
                                .build()
                );
            }

            JsonNode root =
                    objectMapper.readTree(rawChunk);

            JsonNode choices =
                    root.path("choices");

            if (!choices.isArray()
                    || choices.isEmpty()) {

                return Flux.empty();
            }

            JsonNode choice = choices.get(0);

            /*
             * finish_reason chunk
             */
            JsonNode finishReason =
                    choice.get("finish_reason");

            if (finishReason != null
                    && !finishReason.isNull()) {

                return Flux.just(
                        StreamingChunk.builder()
                                .provider("openai")
                                .model(request.getModel())
                                .completed(true)
                                .build()
                );
            }

            JsonNode delta =
                    choice.path("delta");

            /*
             * Extract content
             */
            String content =
                    delta.path("content")
                            .asText("");

            /*
             * Ignore empty chunks
             */
            if (content.isBlank()) {

                return Flux.empty();
            }

            return Flux.just(
                    StreamingChunk.builder()
                            .provider("openai")
                            .model(request.getModel())
                            .content(content)
                            .completed(false)
                            .build()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed parsing OpenAI chunk: {}",
                    rawChunk,
                    ex
            );

            return Flux.empty();
        }
    }
}