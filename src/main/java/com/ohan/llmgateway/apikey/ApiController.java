/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.apikey;

import com.ohan.llmgateway.apikey.service.ApiKeyService;
import com.ohan.llmgateway.auth.entity.User;
import com.ohan.llmgateway.auth.repository.UserRepository;
import com.ohan.llmgateway.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserRepository userRepository;
    private final ApiKeyService apiKeyService;

    @PostMapping("/keys")
    public ResponseEntity<?> createKey(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = auth.getName();

        User user = userRepository.findByEmail(email).orElseThrow();

        String apiKey = apiKeyService.createKey(user);

        return ResponseEntity.ok(Map.of("apiKey", apiKey));
    }
}
