/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.routing.service;

import com.ohan.llmgateway.execution.service.LlmExecutionService;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.AdvancedModelRouter;
import com.ohan.llmgateway.routing.dto.RoutingDecision;
import com.ohan.llmgateway.routing.dto.RoutingRequest;
import com.ohan.llmgateway.routing.engine.CapabilityRouter;
import com.ohan.llmgateway.routing.engine.CostBasedRouter;
import com.ohan.llmgateway.routing.engine.LatencyBasedRouter;
import com.ohan.llmgateway.routing.enums.RoutingStrategy;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutingOrchestrator {

    private final CostBasedRouter costBasedRouter;

    private final LatencyBasedRouter latencyBasedRouter;

    private final CapabilityRouter capabilityRouter;

    private final LlmExecutionService llmExecutionService;

    public LlmResponse route(
            RoutingRequest request,
            String prompt
    ) {

        RoutingDecision decision =
                resolveDecision(request);

        return llmExecutionService.execute(
                decision.getSelectedModel(),
                prompt
        );
    }

    public RoutingDecision resolveDecision(
            RoutingRequest request
    ) {

        RoutingStrategy strategy =
                request.getStrategy();

        return switch (strategy) {

            case CHEAPEST ->
                    costBasedRouter.route();

            case FASTEST ->
                    latencyBasedRouter.route();

            case CAPABILITY_BASED ->
                    capabilityRouter.route(
                            request.getRequestType()
                    );
        };
    }
}
