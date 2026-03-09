/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.usage.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CostCalculator {

    public BigDecimal calculate(String model, int inputTokens, int outputTokens) {

        double inputPrice = 0.005 / 1000;
        double outputPrice = 0.015 / 1000;

        double cost = (inputTokens * inputPrice) + (outputTokens * outputPrice);

        return BigDecimal.valueOf(cost);
    }
}