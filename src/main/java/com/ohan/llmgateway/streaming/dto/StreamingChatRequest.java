/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.dto;

import lombok.Data;

@Data
public class StreamingChatRequest {

    private String provider;
    private String model;
    private String prompt;

}
