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
import com.ohan.llmgateway.security.CurrentUserProvider;
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
    private final StreamingUsageRecorder streamingUsageRecorder;
    private final CurrentUserProvider currentUserProvider;

    public Flux<StreamingChunk> stream(StreamingChatRequest request) {

        // Capture identity now (request thread) for usage attribution on completion
        Long userId = currentUserProvider.currentUserId();
        Long apiKeyId = currentUserProvider.currentApiKeyId();

        String cacheKey = promptCacheService.streamKey(
                request.getProvider(),
                request.getModel(),
                request.getPrompt()
        );

        Flux<StreamingChunk> source;

        // Replay a cached stream if available, otherwise stream from the provider
        Flux<StreamingChunk> cached = promptCacheService.getStream(cacheKey);
        if (cached != null) {
            source = cached;
        } else {
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
            source = promptCacheService.cacheStream(
                    cacheKey,
                    request.getProvider(),
                    request.getModel(),
                    provider.stream(request)
            );
        }

        // Central usage tracking (recorded for both fresh and replayed streams)
        return streamingUsageRecorder.track(
                userId,
                apiKeyId,
                request.getProvider(),
                request.getModel(),
                request.getPrompt(),
                source
        );
    }
}
