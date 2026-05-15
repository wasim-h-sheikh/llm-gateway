/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.model.controller;

import com.ohan.llmgateway.model.entity.ModelMetadata;
import com.ohan.llmgateway.model.service.ModelMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/models")
@RequiredArgsConstructor
public class ModelAdminController {

    private final ModelMetadataService modelMetadataService;

    @PostMapping
    public ModelMetadata createModel(
            @RequestBody ModelMetadata request
    ) {

        return modelMetadataService.save(request);
    }

    @GetMapping
    public List<ModelMetadata> getAllModels() {

        return modelMetadataService.getEnabledModels();
    }

    @PatchMapping("/{id}/disable")
    public String disableModel(
            @PathVariable Long id
    ) {

        modelMetadataService.disableModel(id);

        return "Model disabled successfully";
    }
}