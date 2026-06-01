/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.usage.service;

import com.ohan.llmgateway.usage.entity.UsageLog;
import com.ohan.llmgateway.usage.repository.UsageLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsageService {

    private final UsageLogRepository usageLogRepository;
    private final CostCalculator costCalculator;

    public UsageService(UsageLogRepository usageLogRepository,
                        CostCalculator costCalculator) {
        this.usageLogRepository = usageLogRepository;
        this.costCalculator = costCalculator;
    }

    public void recordUsage(
            Long userId,
            Long apiKeyId,
            String model,
            String provider,
            int inputTokens,
            int outputTokens
    ) {

        UsageLog log = new UsageLog();

        log.setUserId(userId);
        log.setApiKeyId(apiKeyId);
        log.setModel(model);
        log.setProvider(provider);

        log.setInputTokens(inputTokens);
        log.setOutputTokens(outputTokens);
        log.setTotalTokens(inputTokens + outputTokens);

        log.setCostUsd(
                costCalculator.calculate(model, inputTokens, outputTokens)
        );

        log.setRequestId(UUID.randomUUID().toString());

        usageLogRepository.save(log);
    }

    /**
     * Records usage when exact token counts are not available (e.g. streaming
     * responses, whose chunks carry no usage metadata). Tokens are estimated
     * from the input and output text. userId/apiKeyId are not yet wired in.
     */
    public void recordEstimatedUsage(
            String provider,
            String model,
            String inputText,
            String outputText
    ) {
        recordUsage(
                null,
                null,
                model,
                provider,
                estimateTokens(inputText),
                estimateTokens(outputText)
        );
    }

    /**
     * Rough token estimate (~4 characters per token) used when a provider does
     * not return exact token counts. Good enough for usage/cost tracking.
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }
}