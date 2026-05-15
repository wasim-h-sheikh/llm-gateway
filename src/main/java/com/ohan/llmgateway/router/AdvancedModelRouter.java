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

package com.ohan.llmgateway.router;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.service.ModelMetadataService;
import com.ohan.llmgateway.provider.LlmProvider;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.provider.registry.ProviderRegistry;
import com.ohan.llmgateway.resilience.ResilientProviderExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedModelRouter {

    private final ProviderRegistry providerRegistry;
    private final ResilientProviderExecutor executor;
    private final ModelMetadataService modelMetadataService;

    public LlmResponse route(
            String model,
            String prompt
    ) {

        ModelMetadata metadata =
                modelMetadataService.getByModelName(model);

        validateModel(metadata);

        String providerBeanName =
                metadata.getProviderBeanName();

        try {

            LlmProvider provider =
                    providerRegistry.getProvider(
                            providerBeanName
                    );

            return executor.execute(
                    providerBeanName,
                    provider,
                    model,
                    prompt
            );

        } catch (Exception e) {

            log.error(
                    "Provider execution failed. model={} provider={}",
                    model,
                    providerBeanName,
                    e
            );

            throw new RuntimeException(
                    "Provider execution failed",
                    e
            );
        }
    }

    private void validateModel(
            ModelMetadata metadata
    ) {

        if (metadata == null) {
            throw new RuntimeException(
                    "Model metadata not found"
            );
        }

        if (!Boolean.TRUE.equals(metadata.getEnabled())) {
            throw new RuntimeException(
                    "Model is disabled: "
                            + metadata.getModelName()
            );
        }
    }
}