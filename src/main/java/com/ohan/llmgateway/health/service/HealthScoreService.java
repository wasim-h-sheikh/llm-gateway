/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.health.service;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.repository.ModelMetadataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthScoreService {

    private final ModelMetadataRepository repository;

    public void updateHealthScore(
            String modelName,
            boolean success,
            long latencyMs,
            boolean timeout
    ) {

        ModelMetadata model =
                repository.findByModelName(modelName)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Model not found: "
                                                + modelName
                                )
                        );

        updateCounters(
                model,
                success,
                latencyMs,
                timeout
        );

        double score =
                calculateHealthScore(model);

        model.setHealthScore(score);

        repository.save(model);

        log.info(
                "Updated health score. model={} score={}",
                modelName,
                score
        );
    }

    private void updateCounters(
            ModelMetadata model,
            boolean success,
            long latencyMs,
            boolean timeout
    ) {

        long total =
                safe(model.getTotalRequests()) + 1;

        model.setTotalRequests(total);

        if (success) {

            model.setSuccessfulRequests(
                    safe(model.getSuccessfulRequests()) + 1
            );

        } else {

            model.setFailedRequests(
                    safe(model.getFailedRequests()) + 1
            );
        }

        if (timeout) {

            model.setTimeoutCount(
                    safe(model.getTimeoutCount()) + 1
            );
        }

        updateAverageLatency(
                model,
                latencyMs
        );
    }

    private void updateAverageLatency(
            ModelMetadata model,
            long latencyMs
    ) {

        double currentAvg =
                safeDouble(
                        model.getAverageResponseTimeMs()
                );

        long total =
                safe(model.getTotalRequests());

        double updatedAvg =
                ((currentAvg * (total - 1))
                        + latencyMs)
                        / total;

        model.setAverageResponseTimeMs(
                updatedAvg
        );
    }

    private double calculateHealthScore(
            ModelMetadata model
    ) {

        long total =
                safe(model.getTotalRequests());

        if (total == 0) {
            return 100.0;
        }

        double successRate =
                (safe(model.getSuccessfulRequests())
                        * 100.0) / total;

        double timeoutPenalty =
                safe(model.getTimeoutCount()) * 2.0;

        double failurePenalty =
                safe(model.getFailedRequests()) * 1.5;

        double latencyPenalty =
                safeDouble(
                        model.getAverageResponseTimeMs()
                ) / 1000.0;

        double score =
                successRate
                        - timeoutPenalty
                        - failurePenalty
                        - latencyPenalty;

        return Math.max(0, Math.min(100, score));
    }

    private long safe(Long value) {
        return value == null ? 0 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
