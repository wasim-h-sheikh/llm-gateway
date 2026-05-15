/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.model.entity;

import com.ohan.llmgateway.model.enums.ModelCapability;
import com.ohan.llmgateway.model.enums.ProviderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "model_metadata")
@Setter
@Getter
public class ModelMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    private Double inputCostPer1k;

    private Double outputCostPer1k;

    private Integer avgLatencyMs;

    private Integer contextWindow;

    private Boolean supportsStreaming;

    private Boolean supportsTools;

    private Boolean enabled;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "model_capabilities")
    @Enumerated(EnumType.STRING)
    private Set<ModelCapability> capabilities = new HashSet<>();

    private Integer priority;

    private Double healthScore;

    private Instant createdAt;

    private Instant updatedAt;
}