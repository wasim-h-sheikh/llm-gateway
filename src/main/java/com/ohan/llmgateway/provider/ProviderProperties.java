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

package com.ohan.llmgateway.provider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderProperties {

    private String apiKey;

    private String baseUrl;

    private boolean enabled;

}