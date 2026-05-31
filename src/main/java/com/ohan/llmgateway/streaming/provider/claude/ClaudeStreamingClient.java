/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.provider.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohan.llmgateway.provider.ProviderProperties;
import com.ohan.llmgateway.provider.ProvidersConfig;
import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.provider.StreamingProviderClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
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
public class ClaudeStreamingClient implements StreamingProviderClient {

    private final WebClient webClient;
    private final ProvidersConfig providersConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String provider) {
        return "anthropic".equalsIgnoreCase(provider);
    }

    @Override
    public Flux<StreamingChunk> stream(
            StreamingChatRequest request
    ) {

        ProviderProperties config =
                providersConfig.getConfigs()
                        .get("claude");

        if (config == null || !config.isEnabled()) {

            return Flux.error(
                    new RuntimeException(
                            "Claude provider disabled"
                    )
            );
        }

        Map<String, Object> body = Map.of(
                "model", request.getModel(),
                "max_tokens", 4096,
                "stream", true,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", request.getPrompt()
                        )
                )
        );

        log.info(
                "Starting Claude stream. Model={}",
                request.getModel()
        );

        return webClient.post()
                .uri(config.getBaseUrl())
                .header(
                        "x-api-key",
                        config.getApiKey()
                )
                .header(
                        "anthropic-version",
                        "2023-06-01"
                )
                .header(
                        HttpHeaders.ACCEPT,
                        MediaType.TEXT_EVENT_STREAM_VALUE
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .flatMap(node ->
                        parseChunk(
                                node,
                                request
                        )
                )
                .doOnCancel(() ->
                        log.info(
                                "Claude stream cancelled"
                        )
                )
                .doOnComplete(() ->
                        log.info(
                                "Claude stream completed"
                        )
                )
                .onErrorResume(ex -> {

                    log.error(
                            "Claude streaming failed",
                            ex
                    );

                    return Flux.just(
                            StreamingChunk.builder()
                                    .provider("anthropic")
                                    .model(
                                            request.getModel()
                                    )
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

            String type =
                    root.path("type")
                            .asText("");

            /*
             * Final event
             */
            if ("message_stop".equals(type)) {

                return Flux.just(
                        StreamingChunk.builder()
                                .provider("claude")
                                .model(request.getModel())
                                .completed(true)
                                .build()
                );
            }

            /*
             * Ignore:
             *
             * message_start
             * content_block_start
             * content_block_stop
             * ping
             * message_delta
             */
            if (!"content_block_delta".equals(type)) {

                return Flux.empty();
            }

            String content =
                    root.path("delta")
                            .path("text")
                            .asText("");

            if (content.isBlank()) {

                return Flux.empty();
            }

            return Flux.just(
                    StreamingChunk.builder()
                            .provider("claude")
                            .model(request.getModel())
                            .content(content)
                            .completed(false)
                            .build()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed parsing Claude chunk",
                    ex
            );

            return Flux.empty();
        }
    }
}
