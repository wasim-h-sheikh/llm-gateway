/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider.openai;

import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import org.springframework.stereotype.Service;

@Service
public class OpenAiProvider implements LlmProvider {

    @Override
    public LlmResponse generate(String model, String prompt) {

        // TODO: Replace this stub with real OpenAI API call

        int inputTokens = estimateTokens(prompt);
        int outputTokens = 200; // simulated response size

        return LlmResponse.builder()
                .content("Stub AI response for prompt: " + prompt)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .provider("openai")
                .model(model)
                .build();
    }

    /**
     * Temporary token estimation.
     * Later we will replace with jtokkit tokenizer.
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return text.split("\\s+").length * 2;
    }
}