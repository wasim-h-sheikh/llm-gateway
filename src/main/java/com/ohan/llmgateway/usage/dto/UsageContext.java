/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.usage.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Everything needed to record a single usage event. Built by callers and passed
 * to {@code UsageService.recordUsage}. {@code userId}/{@code apiKeyId} may be
 * null for unauthenticated requests; {@code requestId} is generated when absent.
 */
@Getter
@Builder
public class UsageContext {

    private final Long userId;

    private final Long apiKeyId;

    private final String requestId;

    private final String provider;

    private final String model;

    private final int inputTokens;

    private final int outputTokens;
}
