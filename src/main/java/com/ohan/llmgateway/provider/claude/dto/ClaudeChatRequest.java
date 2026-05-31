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

package com.ohan.llmgateway.provider.claude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body for the Anthropic (Claude) official Messages API:
 *
 * <pre>
 * {
 *   "model": "claude-haiku-4-5",
 *   "max_tokens": 4096,
 *   "messages": [
 *     { "role": "user", "content": "Hello" }
 *   ]
 * }
 * </pre>
 *
 * Note: {@code max_tokens} is required by the Anthropic API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaudeChatRequest {

    private String model;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private List<Message> messages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {

        /**
         * Either "user" or "assistant".
         */
        private String role;

        private String content;
    }
}
