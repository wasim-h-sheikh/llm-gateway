/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.model.repository;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelMetadataRepository
        extends JpaRepository<ModelMetadata, Long> {

    Optional<ModelMetadata> findByModelName(String modelName);

    List<ModelMetadata> findByEnabledTrue();

    List<ModelMetadata> findByProviderAndEnabledTrue(
            ProviderType provider
    );
}
