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
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "model_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType provider;

    /**
     * Actual Spring bean name used by ProviderRegistry
     * Examples:
     * openAiProvider
     * nvidiaProvider
     * anthropicProvider
     */
    @Column(nullable = false)
    private String providerBeanName;

    private Double inputCostPer1k;

    private Double outputCostPer1k;

    private Integer avgLatencyMs;

    private Integer contextWindow;

    private Boolean supportsStreaming;

    private Boolean supportsTools;

    private Boolean enabled;

    private Integer priority;

    private Double healthScore;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "model_capabilities",
            joinColumns = @JoinColumn(name = "model_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<ModelCapability> capabilities =
            new HashSet<>();

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        if (this.enabled == null) {
            this.enabled = true;
        }

        if (this.healthScore == null) {
            this.healthScore = 100.0;
        }

        if (this.priority == null) {
            this.priority = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}