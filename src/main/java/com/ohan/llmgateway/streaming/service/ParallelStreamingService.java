/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.service;

import com.ohan.llmgateway.streaming.dto.ParallelStreamRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.provider.StreamingProviderClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Fans a single prompt out to multiple models in parallel and merges their
 * token streams into one reactive stream.
 *
 * Each target is resolved to its {@link StreamingProviderClient} and given its
 * own {@link StreamingChatRequest}. The per-target streams are subscribed
 * eagerly via {@link Flux#merge} so providers emit concurrently and chunks
 * interleave as they arrive. Failures are isolated per target: an unsupported
 * provider or a stream error becomes a terminal error {@link StreamingChunk}
 * for that model and never tears down the other streams.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParallelStreamingService {

    private final List<StreamingProviderClient> providers;

    public Flux<StreamingChunk> stream(ParallelStreamRequest request) {

        if (request.getTargets() == null
                || request.getTargets().isEmpty()) {

            return Flux.error(
                    new RuntimeException(
                            "At least one target model is required"
                    )
            );
        }

        if (request.getPrompt() == null
                || request.getPrompt().isBlank()) {

            return Flux.error(
                    new RuntimeException(
                            "Prompt cannot be empty"
                    )
            );
        }

        List<Flux<StreamingChunk>> streams =
                request.getTargets()
                        .stream()
                        .map(target ->
                                streamTarget(
                                        target,
                                        request.getPrompt()
                                )
                        )
                        .toList();

        return Flux.merge(streams);
    }

    private Flux<StreamingChunk> streamTarget(
            ParallelStreamRequest.ModelTarget target,
            String prompt
    ) {

        StreamingProviderClient client =
                providers.stream()
                        .filter(p ->
                                p.supports(target.getProvider())
                        )
                        .findFirst()
                        .orElse(null);

        if (client == null) {

            log.warn(
                    "Unsupported provider in parallel stream: {}",
                    target.getProvider()
            );

            return Flux.just(
                    StreamingChunk.builder()
                            .provider(target.getProvider())
                            .model(target.getModel())
                            .error(true)
                            .errorMessage(
                                    "Unsupported provider: "
                                            + target.getProvider()
                            )
                            .completed(true)
                            .build()
            );
        }

        StreamingChatRequest single = new StreamingChatRequest();
        single.setProvider(target.getProvider());
        single.setModel(target.getModel());
        single.setPrompt(prompt);

        return client.stream(single)
                .onErrorResume(ex -> {

                    log.error(
                            "Parallel stream failed for {}:{}",
                            target.getProvider(),
                            target.getModel(),
                            ex
                    );

                    return Flux.just(
                            StreamingChunk.builder()
                                    .provider(target.getProvider())
                                    .model(target.getModel())
                                    .error(true)
                                    .errorMessage(
                                            ex.getMessage() != null
                                                    ? ex.getMessage()
                                                    : "Unknown streaming error"
                                    )
                                    .completed(true)
                                    .build()
                    );
                });
    }
}
