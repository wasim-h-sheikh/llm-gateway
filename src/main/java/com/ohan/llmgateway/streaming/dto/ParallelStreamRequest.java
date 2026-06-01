/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for streaming the same prompt to multiple models in parallel.
 *
 * Each {@link ModelTarget} names a provider ("openai", "anthropic", "google")
 * and a concrete model. The gateway fans the prompt out to every target and
 * merges their token streams into a single SSE response. The UI demultiplexes
 * chunks back into per-model columns using the {@code provider}/{@code model}
 * fields carried on each {@link StreamingChunk}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParallelStreamRequest {

    private List<ModelTarget> targets;

    private String prompt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelTarget {

        private String provider;

        private String model;
    }
}
