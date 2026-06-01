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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptCacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /**
     * Returns the cached value for the prompt deserialized into the given type,
     * or {@code null} if there is no cache entry (or it cannot be deserialized).
     */
    public <T> T get(String prompt, Class<T> type) {
        String cached = redis.opsForValue().get(key(prompt));
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached, type);
        } catch (Exception e) {
            log.warn("Failed to deserialize cached prompt response; ignoring cache entry", e);
            return null;
        }
    }

    /**
     * Serializes the response to JSON and caches it against the prompt with a 10-minute TTL.
     */
    public void put(String prompt, Object response) {
        try {
            String serialized = objectMapper.writeValueAsString(response);
            redis.opsForValue().set(key(prompt), serialized, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to serialize response for caching; skipping cache write", e);
        }
    }

    private String key(String prompt) {
        // Normalize the prompt (lowercase) so equivalent prompts hit the same cache entry
        String normalized = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        return "cache:prompt:" + hash(normalized);
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