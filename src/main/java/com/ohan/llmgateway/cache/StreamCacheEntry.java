/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cached representation of a completed streamed response. Stores the provider,
 * model and the fully accumulated content so the stream can be replayed from
 * cache without calling the provider again.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamCacheEntry {

    private String provider;

    private String model;

    private String content;
}
