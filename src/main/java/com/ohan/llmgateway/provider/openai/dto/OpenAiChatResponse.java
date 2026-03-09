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

package com.ohan.llmgateway.provider.openai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAiChatResponse {

    private List<Choice> choices;

    private Usage usage;

    @Getter
    @Setter
    public static class Choice {

        private Message message;
    }

    @Getter
    @Setter
    public static class Message {

        private String role;

        private String content;
    }

    @Getter
    @Setter
    public static class Usage {

        private int prompt_tokens;

        private int completion_tokens;

        private int total_tokens;
    }
}