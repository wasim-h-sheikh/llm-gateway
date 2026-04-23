/*
 *
 *  *
 *  *  * Copyright (c) 2026 Wasim Sheikh
 *  *  * Project: LLM Gateway
 *  *  *
 *  *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  *  * Proprietary and confidential.
 *  *
 *
 *
 */

package com.ohan.llmgateway.chat.controller;

import com.ohan.llmgateway.chat.dto.ChatCompletionRequest;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.MultiModelExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/multi")
@RequiredArgsConstructor
public class MultiModelController {

    private final MultiModelExecutor executor;

    @PostMapping("/compare")
    public Map<String, LlmResponse> compare(
            @RequestBody ChatCompletionRequest request,
            @RequestParam List<String> providers
    ) {

        String prompt = request.getMessages()
                .stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .reduce("", (a, b) -> a + "\n" + b);

        return executor.executeParallel(
                providers,
                request.getModel(),
                prompt
        );
    }
}