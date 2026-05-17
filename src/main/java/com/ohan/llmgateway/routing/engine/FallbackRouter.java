/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.routing.engine;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.repository.ModelMetadataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FallbackRouter {

    private final ModelMetadataRepository repository;

    public List<ModelMetadata> getFallbackChain() {

        return repository.findByEnabledTrue()
                .stream()
                .sorted((a, b) ->
                        Integer.compare(
                                a.getPriority(),
                                b.getPriority()
                        )
                )
                .toList();
    }
}
