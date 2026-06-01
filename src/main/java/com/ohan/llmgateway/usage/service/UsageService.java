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

import com.ohan.llmgateway.usage.dto.UsageContext;
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

    /**
     * Records a usage event from a {@link UsageContext}. Token counts and
     * provider/model come from the caller; cost is derived; requestId is
     * generated when not supplied.
     */
    public void recordUsage(UsageContext context) {

        int inputTokens = context.getInputTokens();
        int outputTokens = context.getOutputTokens();

        UsageLog log = new UsageLog();

        log.setUserId(context.getUserId());
        log.setApiKeyId(context.getApiKeyId());
        log.setModel(context.getModel());
        log.setProvider(context.getProvider());

        log.setInputTokens(inputTokens);
        log.setOutputTokens(outputTokens);
        log.setTotalTokens(inputTokens + outputTokens);

        log.setCostUsd(
                costCalculator.calculate(context.getModel(), inputTokens, outputTokens)
        );

        log.setRequestId(
                context.getRequestId() != null
                        ? context.getRequestId()
                        : UUID.randomUUID().toString()
        );

        usageLogRepository.save(log);
    }
}
