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

import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.usage.dto.UsageContext;
import com.ohan.llmgateway.usage.service.UsageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central usage tracking for streamed responses. Streaming chunks carry no
 * token counts, so this wrapper accumulates the streamed content and records
 * estimated usage once the stream completes successfully. Shared by the single
 * and parallel streaming services so usage tracking lives in one place.
 *
 * <p>userId/apiKeyId must be captured by the caller on the request thread (the
 * SecurityContext is not available in the deferred completion callback).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StreamingUsageRecorder {

    private final UsageService usageService;

    /**
     * Wraps {@code source} so its content is accumulated and recorded as usage
     * on successful completion. If any chunk carries an error, nothing is
     * recorded. The returned {@link Flux} is otherwise identical to {@code source}.
     */
    public Flux<StreamingChunk> track(
            Long userId,
            Long apiKeyId,
            String provider,
            String model,
            String prompt,
            Flux<StreamingChunk> source
    ) {
        StringBuilder output = new StringBuilder();
        AtomicBoolean failed = new AtomicBoolean(false);

        return source
                .doOnNext(chunk -> {
                    if (chunk.isError()) {
                        failed.set(true);
                    } else if (chunk.getContent() != null) {
                        output.append(chunk.getContent());
                    }
                })
                .doOnComplete(() -> {
                    if (failed.get()) {
                        return;
                    }
                    usageService.recordUsage(
                            UsageContext.builder()
                                    .userId(userId)
                                    .apiKeyId(apiKeyId)
                                    .provider(provider)
                                    .model(model)
                                    .inputTokens(estimateTokens(prompt))
                                    .outputTokens(estimateTokens(output.toString()))
                                    .build()
                    );
                    log.info(
                            "Usage recorded (stream, estimated). provider={} model={}",
                            provider,
                            model
                    );
                });
    }

    /**
     * Rough token estimate (~4 characters per token), used because streaming
     * providers do not return exact token counts.
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }
}
