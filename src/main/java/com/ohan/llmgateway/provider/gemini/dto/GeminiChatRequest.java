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

package com.ohan.llmgateway.provider.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request body for the Gemini official API generateContent endpoint:
 *
 * <pre>
 * {
 *   "contents": [
 *     { "role": "user", "parts": [ { "text": "Hello" } ] }
 *   ]
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiChatRequest {

    private List<Content> contents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {

        /**
         * Either "user" or "model". Optional for single-turn requests,
         * but kept explicit for clarity and multi-turn support.
         */
        private String role;

        private List<Part> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {

        private String text;
    }
}
