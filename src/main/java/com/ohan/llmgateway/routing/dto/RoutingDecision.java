/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.routing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutingDecision {

    private String selectedModel;

    private String provider;

    private String reason;
}
