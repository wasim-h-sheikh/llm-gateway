/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.apikey.service;

import com.ohan.llmgateway.apikey.entity.ApiKey;
import com.ohan.llmgateway.apikey.repository.ApiKeyRepository;
import com.ohan.llmgateway.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator generator;
    private final PasswordEncoder passwordEncoder;

    public String createKey(User user) {

        String rawKey = generator.generateKey();

        String prefix = rawKey.substring(0, 12);

        ApiKey apiKey = ApiKey.builder()
                .keyHash(passwordEncoder.encode(rawKey))
                .keyPrefix(prefix)
                .user(user)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        apiKeyRepository.save(apiKey);

        return rawKey;
    }
}