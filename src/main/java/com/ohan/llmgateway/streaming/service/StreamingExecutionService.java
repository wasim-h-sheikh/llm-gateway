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

import com.ohan.llmgateway.cache.PromptCacheService;
import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.provider.StreamingProviderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingExecutionService {

    private final List<StreamingProviderClient> providers;
    private final PromptCacheService promptCacheService;

    public Flux<StreamingChunk> stream(StreamingChatRequest request) {

        String cacheKey = promptCacheService.streamKey(
                request.getProvider(),
                request.getModel(),
                request.getPrompt()
        );

        // Replay a cached stream if available
        Flux<StreamingChunk> cached = promptCacheService.getStream(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info(
                "Prompt cache MISS (stream) - streaming from provider. provider={} model={}",
                request.getProvider(),
                request.getModel()
        );

        StreamingProviderClient provider = providers.stream()
                .filter(p -> p.supports(request.getProvider()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unsupported provider: " + request.getProvider()
                        )
                );

        // Accumulate and cache the stream on successful completion
        return promptCacheService.cacheStream(
                cacheKey,
                request.getProvider(),
                request.getModel(),
                provider.stream(request)
        );
    }
}
