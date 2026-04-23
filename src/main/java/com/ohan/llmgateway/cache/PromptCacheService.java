/*
 *
 *  *
 *  *  * Copyright (c) 2026 Wasim Sheikh
 *  *  * Project: LLM Gateway
 *  *  *
 *  *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  *  * Proprietary and confidential.
 *  *
 *
 *
 */

package com.ohan.llmgateway.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PromptCacheService {

    private final StringRedisTemplate redis;

    public String get(String prompt) {
        return redis.opsForValue().get(key(prompt));
    }

    public void put(String prompt, String response) {
        redis.opsForValue().set(key(prompt), response, 10, TimeUnit.MINUTES);
    }

    private String key(String prompt) {
        return "cache:prompt:" + hash(prompt);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(input.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}