/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.streaming.provider;

import com.ohan.llmgateway.streaming.dto.StreamingChatRequest;
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import reactor.core.publisher.Flux;

public interface StreamingProviderClient {

    Flux<StreamingChunk> stream(StreamingChatRequest request);

    boolean supports(String provider);

}