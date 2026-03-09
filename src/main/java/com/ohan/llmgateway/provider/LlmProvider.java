/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.provider;

import com.ohan.llmgateway.provider.dto.LlmResponse;

public interface LlmProvider {

    LlmResponse generate(String model, String prompt);
}