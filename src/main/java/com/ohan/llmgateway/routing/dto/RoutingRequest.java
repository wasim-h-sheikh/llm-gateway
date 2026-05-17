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

import com.ohan.llmgateway.routing.enums.RequestType;
import com.ohan.llmgateway.routing.enums.RoutingStrategy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutingRequest {

    private RoutingStrategy strategy;

    private RequestType requestType;

    private Boolean streaming;

    private Boolean toolsRequired;
}
