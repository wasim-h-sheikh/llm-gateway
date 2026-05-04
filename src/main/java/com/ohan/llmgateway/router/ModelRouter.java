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
import com.ohan.llmgateway.provider.nvidia.NvidiaProvider;
import com.ohan.llmgateway.provider.openai.OpenAiProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ModelRouter {

    private final OpenAiProvider openAiProvider;
    private final NvidiaProvider nvidiaProvider;

    public ModelRouter(OpenAiProvider openAiProvider, NvidiaProvider nvidiaProvider) {
        this.openAiProvider = openAiProvider;
        this.nvidiaProvider = nvidiaProvider;
    }

    public LlmResponse route(String model, String prompt) {
        log.info("ModelRouter:route");

        if (model.contains("/")) {
            return nvidiaProvider.generate(model, prompt);
        }

        if (model.startsWith("gpt")) {
            return openAiProvider.generate(model, prompt);
        }

        throw new RuntimeException("Model not supported: " + model);
    }
}