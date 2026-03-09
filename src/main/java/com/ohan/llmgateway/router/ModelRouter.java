/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.router;

import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.provider.openai.OpenAiProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ModelRouter {

    private final OpenAiProvider openAiProvider;

    public ModelRouter(OpenAiProvider openAiProvider) {
        this.openAiProvider = openAiProvider;
    }

    public LlmResponse route(String model, String prompt) {
        log.info("ModelRouter:route");
        if (model.startsWith("gpt")) {
            return openAiProvider.generate(model, prompt);
        }

        throw new RuntimeException("Model not supported: " + model);
    }
}