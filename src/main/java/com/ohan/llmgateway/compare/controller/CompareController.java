/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.compare.controller;

import com.ohan.llmgateway.compare.dto.CompareRequest;
import com.ohan.llmgateway.compare.dto.CompareResponse;
import com.ohan.llmgateway.compare.service.MultiModelCompareService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class CompareController {

    private final MultiModelCompareService compareService;

    @PostMapping("/compare")
    public CompareResponse compare(
            @RequestBody CompareRequest request
    ) {

        return compareService.compare(request);
    }
}
