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
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.ModelRouter;
import com.ohan.llmgateway.usage.service.UsageService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class StreamingChatController {

    private final ModelRouter modelRouter;
    private final UsageService usageService;

    @PostMapping(value = "/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatCompletionRequest request) {

        SseEmitter emitter = new SseEmitter(0L);

        new Thread(() -> {
            try {

                String prompt = buildPrompt(request);

                LlmResponse response = modelRouter.route(
                        request.getModel(),
                        prompt
                );

                String content = response.getContent();
                String[] tokens = content.split(" ");

                for (String token : tokens) {

                    String event = buildChunk(token);

                    emitter.send(event);

                    Thread.sleep(80); // simulate token streaming
                }

                emitter.send("data: [DONE]\n\n");

                usageService.recordUsage(
                        null,
                        null,
                        response.getModel(),
                        response.getProvider(),
                        response.getInputTokens(),
                        response.getOutputTokens()
                );

                emitter.complete();

            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private String buildPrompt(ChatCompletionRequest request) {

        StringBuilder builder = new StringBuilder();

        request.getMessages().forEach(m ->
                builder.append(m.getRole())
                        .append(": ")
                        .append(m.getContent())
                        .append("\n")
        );

        return builder.toString();
    }

    private String buildChunk(String token) {

        String id = "chatcmpl-" + UUID.randomUUID();

        return "data: {\"id\":\"" + id + "\"," +
                "\"object\":\"chat.completion.chunk\"," +
                "\"choices\":[{" +
                "\"delta\":{\"content\":\"" + token + " \"}," +
                "\"index\":0" +
                "}]" +
                "}\n\n";
    }
}