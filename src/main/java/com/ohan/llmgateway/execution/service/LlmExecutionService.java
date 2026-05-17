/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.execution.service;

import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.ModelRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmExecutionService {

    private final ModelRouter modelRouter;

    public LlmResponse execute(
            String model,
            String prompt
    ) {

        log.info(
                "Executing model request. model={}",
                model
        );

        return modelRouter.route(
                model,
                prompt
        );
    }
}
