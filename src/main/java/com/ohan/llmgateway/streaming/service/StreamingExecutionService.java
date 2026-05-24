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

import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.provider.StreamingProviderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamingExecutionService {

    private final List<StreamingProviderClient> providers;

    public Flux<StreamingChunk> stream(StreamingChatRequest request) {

        StreamingProviderClient provider = providers.stream()
                .filter(p -> p.supports(request.getProvider()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unsupported provider: " + request.getProvider()
                        )
                );

        return provider.stream(request);
    }
}
