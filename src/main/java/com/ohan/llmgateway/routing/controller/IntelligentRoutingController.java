/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.routing.controller;

import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.routing.dto.IntelligentRoutingChatRequest;
import com.ohan.llmgateway.routing.dto.IntelligentRoutingResponse;
import com.ohan.llmgateway.routing.dto.RoutingDecision;
import com.ohan.llmgateway.routing.dto.RoutingRequest;
import com.ohan.llmgateway.routing.service.RoutingOrchestrator;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class IntelligentRoutingController {

    private final RoutingOrchestrator routingOrchestrator;

    @PostMapping("/route")
    public IntelligentRoutingResponse route(
            @RequestBody IntelligentRoutingChatRequest request
    ) {

        String prompt = extractPrompt(request);

        RoutingDecision decision =
                routingOrchestrator.resolveDecision(
                        request.getRouting()
                );

        LlmResponse response =
                routingOrchestrator.route(
                        request.getRouting(),
                        prompt
                );

        return IntelligentRoutingResponse.builder()
                .selectedModel(
                        decision.getSelectedModel()
                )
                .provider(
                        decision.getProvider()
                )
                .reason(
                        decision.getReason()
                )
                .response(
                        response.getContent()
                )
                .build();
    }

    private String extractPrompt(
            IntelligentRoutingChatRequest request
    ) {

        if (request.getMessages() == null
                || request.getMessages().isEmpty()) {

            throw new RuntimeException(
                    "Messages cannot be empty"
            );
        }

        return request.getMessages()
                .get(0)
                .getContent();
    }
}
