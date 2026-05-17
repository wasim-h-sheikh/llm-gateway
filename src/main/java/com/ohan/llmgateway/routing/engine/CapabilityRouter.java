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
                                Comparator.comparing(
                                        ModelMetadata::getAvgLatencyMs
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
                        "Capability optimized routing"
                )
                .build();
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
}
