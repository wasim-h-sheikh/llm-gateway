/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int API_KEY_LIMIT = 20;
    private static final int IP_LIMIT = 100;

    public boolean isAllowed(String key, int limit) {

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        return count <= limit;
    }

    public boolean isApiKeyAllowed(String prefix) {
        return isAllowed("rate_limit:apikey:" + prefix, API_KEY_LIMIT);
    }

    public boolean isIpAllowed(String ip) {
        return isAllowed("rate_limit:ip:" + ip, IP_LIMIT);
    }
}