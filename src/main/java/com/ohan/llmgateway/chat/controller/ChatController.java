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

import com.ohan.llmgateway.chat.dto.ChatCompletionRequest;
import com.ohan.llmgateway.chat.dto.ChatCompletionResponse;
import com.ohan.llmgateway.execution.service.LlmExecutionService;
import com.ohan.llmgateway.provider.dto.LlmResponse;
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

    @PostMapping("/completions")
    public ResponseEntity<ChatCompletionResponse> chatCompletion(
            @RequestBody ChatCompletionRequest request
    ) {

        String prompt = buildPrompt(request);
        log.info("ChatController:chatCompletion");

        LlmResponse response = llmExecutionService.execute(
                request.getModel(),
                prompt
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