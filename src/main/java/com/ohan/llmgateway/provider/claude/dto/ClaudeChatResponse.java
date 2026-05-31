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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response body for the Anthropic (Claude) official Messages API:
 *
 * <pre>
 * {
 *   "id": "msg_...",
 *   "type": "message",
 *   "role": "assistant",
 *   "model": "claude-haiku-4-5",
 *   "content": [ { "type": "text", "text": "..." } ],
 *   "stop_reason": "end_turn",
 *   "usage": { "input_tokens": 10, "output_tokens": 20 }
 * }
 * </pre>
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeChatResponse {

    private String id;

    private String model;

    private String role;

    @JsonProperty("stop_reason")
    private String stopReason;

    private List<ContentBlock> content;

    private Usage usage;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentBlock {

        private String type;

        private String text;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        @JsonProperty("input_tokens")
        private int inputTokens;

        @JsonProperty("output_tokens")
        private int outputTokens;
    }
}
