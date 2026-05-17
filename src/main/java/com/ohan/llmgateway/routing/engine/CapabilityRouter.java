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
import com.ohan.llmgateway.model.enums.ModelCapability;
import com.ohan.llmgateway.model.repository.ModelMetadataRepository;
import com.ohan.llmgateway.routing.dto.RoutingDecision;
import com.ohan.llmgateway.routing.enums.RequestType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CapabilityRouter {

    private final ModelMetadataRepository repository;

    public RoutingDecision route(
            RequestType requestType
    ) {

        List<ModelMetadata> models =
                repository.findByEnabledTrue();

        ModelCapability capability =
                mapRequestType(requestType);

        ModelMetadata selected =
                models.stream()
                        .filter(model ->
                                model.getCapabilities()
                                        .contains(capability)
                        )
                        .min(
                                Comparator.comparingDouble(
                                        this::calculateScore
                                )
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No model found for capability: "
                                                + capability
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
                        buildReason(
                                selected,
                                capability
                        )
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
                safeDouble(
                        model.getHealthScore()
                ) * 5;

        return latency - healthBonus;
    }

    private String buildReason(
            ModelMetadata model,
            ModelCapability capability
    ) {

        return String.format(
                "Capability routing " +
                        "(capability=%s latency=%sms health=%.2f)",
                capability,
                model.getAvgLatencyMs(),
                model.getHealthScore()
        );
    }

    private ModelCapability mapRequestType(
            RequestType type
    ) {

        return switch (type) {

            case CODING ->
                    ModelCapability.CODING;

            case REASONING ->
                    ModelCapability.REASONING;

            case VISION ->
                    ModelCapability.VISION;

            default ->
                    ModelCapability.CHAT;
        };
    }

    private int safeInt(Integer value) {
        return value == null ? 999999 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0 : value;
    }
}