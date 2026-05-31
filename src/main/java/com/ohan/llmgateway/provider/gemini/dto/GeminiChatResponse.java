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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response body for the Gemini official API generateContent endpoint:
 *
 * <pre>
 * {
 *   "candidates": [
 *     {
 *       "content": { "parts": [ { "text": "..." } ], "role": "model" },
 *       "finishReason": "STOP"
 *     }
 *   ],
 *   "usageMetadata": {
 *     "promptTokenCount": 10,
 *     "candidatesTokenCount": 20,
 *     "totalTokenCount": 30
 *   }
 * }
 * </pre>
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiChatResponse {

    private List<Candidate> candidates;

    private UsageMetadata usageMetadata;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {

        private Content content;

        private String finishReason;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {

        private List<Part> parts;

        private String role;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {

        private String text;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {

        private int promptTokenCount;

        private int candidatesTokenCount;

        private int totalTokenCount;
    }
}
