/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.common.error;

public class ErrorCode {
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_JWT = "INVALID_JWT";
    public static final String INVALID_API_KEY = "INVALID_API_KEY";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String PROVIDER_ERROR = "PROVIDER_ERROR";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String IP_RATE_LIMIT_EXCEEDED = "IP_RATE_LIMIT_EXCEEDED";
    public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
}