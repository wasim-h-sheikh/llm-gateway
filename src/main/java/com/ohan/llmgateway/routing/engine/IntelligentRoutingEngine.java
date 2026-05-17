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
import com.ohan.llmgateway.routing.dto.RoutingRequest;
import com.ohan.llmgateway.routing.enums.RequestType;
import com.ohan.llmgateway.routing.enums.RoutingStrategy;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntelligentRoutingEngine {

    private final ModelMetadataRepository repository;

    public RoutingDecision route(
            RoutingRequest request
    ) {

        return switch (request.getStrategy()) {

            case FASTEST ->
                    routeFastest();

            case CHEAPEST ->
                    routeCheapest();

            case CAPABILITY_BASED ->
                    routeByCapability(
                            request.getRequestType()
                    );
        };
    }

    private RoutingDecision routeFastest() {

        List<ModelMetadata> models =
                repository
                        .findByEnabledTrueOrderByAvgLatencyMsAsc();

        ModelMetadata selected = models.get(0);

        return buildDecision(
                selected,
                "Lowest latency model"
        );
    }

    private RoutingDecision routeCheapest() {

        List<ModelMetadata> models =
                repository
                        .findByEnabledTrueOrderByInputCostPer1kAsc();

        ModelMetadata selected = models.get(0);

        return buildDecision(
                selected,
                "Lowest cost model"
        );
    }

    private RoutingDecision routeByCapability(
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
                                        "No matching model found"
                                )
                        );

        return buildDecision(
                selected,
                "Capability optimized routing"
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

    private RoutingDecision buildDecision(
            ModelMetadata model,
            String reason
    ) {

        return RoutingDecision.builder()
                .selectedModel(model.getModelName())
                .provider(model.getProvider().name())
                .reason(reason)
                .build();
    }
}
