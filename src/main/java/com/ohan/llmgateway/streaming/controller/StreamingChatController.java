/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.controller;

import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import com.ohan.llmgateway.streaming.service.StreamingExecutionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class StreamingChatController {

    private final StreamingExecutionService
            streamingExecutionService;

    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<StreamingChunk>> stream(
            @RequestBody StreamingChatRequest request
    ) {

        return streamingExecutionService
                .stream(request)

                .map(chunk ->

                        ServerSentEvent
                                .<StreamingChunk>builder()

                                .data(chunk)

                                .build()
                );
    }
}
