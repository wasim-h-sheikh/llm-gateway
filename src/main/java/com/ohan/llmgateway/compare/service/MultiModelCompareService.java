/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.compare.service;

import com.ohan.llmgateway.compare.dto.CompareRequest;
import com.ohan.llmgateway.compare.dto.CompareResponse;
import com.ohan.llmgateway.compare.dto.ModelComparisonResult;
import com.ohan.llmgateway.execution.service.LlmExecutionService;
import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.service.ModelMetadataService;
import com.ohan.llmgateway.provider.dto.LlmResponse;
import com.ohan.llmgateway.router.AdvancedModelRouter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiModelCompareService {

    private final LlmExecutionService llmExecutionService;
    private final ModelMetadataService modelMetadataService;

    public CompareResponse compare(
            CompareRequest request
    ) {

        String prompt = extractPrompt(request);

        // Capture the security context on the request thread so usage recorded
        // on the async worker threads is still attributed to the caller.
        SecurityContext securityContext = SecurityContextHolder.getContext();

        List<CompletableFuture<ModelComparisonResult>> futures =
                request.getModels()
                        .stream()
                        .map(model ->
                                CompletableFuture.supplyAsync(
                                        () -> {
                                            SecurityContextHolder.setContext(securityContext);
                                            try {
                                                return executeModel(
                                                        model,
                                                        prompt
                                                );
                                            } finally {
                                                SecurityContextHolder.clearContext();
                                            }
                                        }
                                )
                        )
                        .toList();

        List<ModelComparisonResult> results =
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

        return CompareResponse.builder()
                .responses(results)
                .build();
    }

    private ModelComparisonResult executeModel(
            String model,
            String prompt
    ) {

        long start = System.currentTimeMillis();

        try {

            ModelMetadata metadata =
                    modelMetadataService.getByModelName(model);

            LlmResponse response =
                    llmExecutionService.execute(model, prompt);

            long latency =
                    System.currentTimeMillis() - start;

            return ModelComparisonResult.builder()
                    .model(model)
                    .provider(
                            metadata.getProvider().name()
                    )
                    .latencyMs(latency)
                    .content(response.getContent())
                    .success(true)
                    .build();

        } catch (Exception e) {

            long latency =
                    System.currentTimeMillis() - start;

            log.error(
                    "Compare execution failed for model: {}",
                    model,
                    e
            );

            return ModelComparisonResult.builder()
                    .model(model)
                    .latencyMs(latency)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    private String extractPrompt(
            CompareRequest request
    ) {

        if (request.getMessages() == null
                || request.getMessages().isEmpty()) {

            throw new RuntimeException(
                    "Messages cannot be empty"
            );
        }

        return request.getMessages()
                .get(0)
                .getContent();
    }
}