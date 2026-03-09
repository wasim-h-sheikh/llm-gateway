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
}