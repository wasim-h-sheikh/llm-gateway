/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.chat.controller;

import com.ohan.llmgateway.cache.PromptCacheService;
import com.ohan.llmgateway.chat.dto.ChatCompletionRequest;
import com.ohan.llmgateway.chat.dto.ChatCompletionResponse;
import com.ohan.llmgateway.execution.service.LlmExecutionService;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.AdvancedModelRouter;
import com.ohan.llmgateway.usage.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final LlmExecutionService llmExecutionService;
    private final UsageService usageService;
    private final PromptCacheService promptCacheService;

    @PostMapping("/completions")
    public ResponseEntity<ChatCompletionResponse> chatCompletion(
            @RequestBody ChatCompletionRequest request
    ) {

        String prompt = buildPrompt(request);
        log.info("ChatController:chatCompletion");

        LlmResponse response;

        // Check prompt cache before hitting the LLM
        LlmResponse cached = promptCacheService.get(prompt, LlmResponse.class);
        if (cached != null) {
            log.info(
                    "Prompt cache HIT - returning cached response. model={} provider={}",
                    cached.getModel(),
                    cached.getProvider()
            );
            response = cached;
        } else {
            log.info("Prompt cache MISS - executing model request. model={}", request.getModel());
            response = llmExecutionService.execute(
                    request.getModel(),
                    prompt
            );

            // Store the full response (content + provider/token metadata) for subsequent identical prompts
            promptCacheService.put(prompt, response);
            log.info("Prompt cache STORE - cached response for prompt. model={}", response.getModel());
        }

        // Save usage (recorded for both fresh and cached responses)
        usageService.recordUsage(
                null,
                null,
                response.getModel(),
                response.getProvider(),
                response.getInputTokens(),
                response.getOutputTokens()
        );

        ChatCompletionResponse chatResponse = buildResponse(
                response.getModel(),
                response.getContent()
        );

        return ResponseEntity.ok(chatResponse);
    }

    private ChatCompletionResponse buildResponse(String model, String content) {
        return ChatCompletionResponse.builder()
                .id("chatcmpl-" + UUID.randomUUID())
                .object("chat.completion")
                .created(Instant.now().getEpochSecond())
                .model(model)
                .choices(List.of(
                        ChatCompletionResponse.Choice.builder()
                                .index(0)
                                .finishReason("stop")
                                .message(
                                        ChatCompletionResponse.Message.builder()
                                                .role("assistant")
                                                .content(content)
                                                .build()
                                )
                                .build()
                ))
                .build();
    }

    private String buildPrompt(ChatCompletionRequest request) {

        StringBuilder builder = new StringBuilder();

        for (ChatCompletionRequest.Message message : request.getMessages()) {
            builder.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        return builder.toString();
    }
}