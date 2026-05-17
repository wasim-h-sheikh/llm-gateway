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

import java.util.List;

@Component
@RequiredArgsConstructor
public class LatencyBasedRouter {

    private final ModelMetadataRepository repository;

    public RoutingDecision route() {

        List<ModelMetadata> models =
                repository
                        .findByEnabledTrueOrderByAvgLatencyMsAsc();

        if (models.isEmpty()) {
            throw new RuntimeException(
                    "No enabled models found"
            );
        }

        ModelMetadata selected = models.get(0);

        return RoutingDecision.builder()
                .selectedModel(
                        selected.getModelName()
                )
                .provider(
                        selected.getProvider().name()
                )
                .reason(
                        "Lowest latency routing"
                )
                .build();
    }
}