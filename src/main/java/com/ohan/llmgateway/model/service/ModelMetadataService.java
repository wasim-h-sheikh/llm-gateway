/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.model.service;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.enums.ProviderType;
import com.ohan.llmgateway.model.repository.ModelMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelMetadataService {

    private final ModelMetadataRepository repository;

    public ModelMetadata getByModelName(String modelName) {

        return repository.findByModelName(modelName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Model metadata not found: " + modelName
                        )
                );
    }

    public List<ModelMetadata> getEnabledModels() {
        return repository.findByEnabledTrue();
    }

    public List<ModelMetadata> getEnabledModelsByProvider(
            ProviderType provider
    ) {
        return repository.findByProviderAndEnabledTrue(provider);
    }

    public ModelMetadata save(ModelMetadata metadata) {
        return repository.save(metadata);
    }

    public void disableModel(Long id) {

        ModelMetadata metadata = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Model not found: " + id
                        )
                );

        metadata.setEnabled(false);

        repository.save(metadata);
    }
}
