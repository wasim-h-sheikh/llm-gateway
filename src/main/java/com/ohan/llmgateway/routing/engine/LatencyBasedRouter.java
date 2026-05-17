/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.routing.engine;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.repository.ModelMetadataRepository;
import com.ohan.llmgateway.routing.dto.RoutingDecision;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LatencyBasedRouter {

    private final ModelMetadataRepository repository;

    public RoutingDecision route() {

        List<ModelMetadata> models =
                repository.findByEnabledTrue();

        if (models.isEmpty()) {

            throw new RuntimeException(
                    "No enabled models found"
            );
        }

        ModelMetadata selected =
                models.stream()
                        .min(
                                Comparator.comparingDouble(
                                        this::calculateScore
                                )
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No healthy model found"
                                )
                        );

        return RoutingDecision.builder()
                .selectedModel(
                        selected.getModelName()
                )
                .provider(
                        selected.getProvider().name()
                )
                .reason(
                        buildReason(selected)
                )
                .build();
    }

    /**
     * Lower score is better
     */
    private double calculateScore(
            ModelMetadata model
    ) {

        double latency =
                safeInt(model.getAvgLatencyMs());

        double healthBonus =
                safeDouble(model.getHealthScore()) * 5;

        return latency - healthBonus;
    }

    private String buildReason(
            ModelMetadata model
    ) {

        return String.format(
                "Latency optimized routing " +
                        "(latency=%sms health=%.2f)",
                model.getAvgLatencyMs(),
                model.getHealthScore()
        );
    }

    private int safeInt(Integer value) {
        return value == null ? 999999 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0 : value;
    }
}