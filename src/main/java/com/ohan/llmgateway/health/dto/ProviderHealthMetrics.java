/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.health.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProviderHealthMetrics {

    private Long totalRequests;

    private Long successfulRequests;

    private Long failedRequests;

    private Long timeoutCount;

    private Double averageLatencyMs;

    private Double successRate;

    private Double failureRate;
}