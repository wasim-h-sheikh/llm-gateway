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
import com.ohan.llmgateway.streaming.dto.StreamingChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central prompt cache shared by every chat path in the gateway.
 *
 * <p>Non-streaming callers (via {@code LlmExecutionService}) use
 * {@link #get(String, Class)} / {@link #put(String, Object)} to cache a whole
 * response object. Streaming callers use {@link #getStream(String)} to replay a
 * cached stream as a {@link Flux} and {@link #cacheStream(String, String, String, Flux)}
 * to transparently accumulate and store a live stream once it completes.
 *
 * <p>Build keys with {@link #completionKey(String, String)} and
 * {@link #streamKey(String, String, String)} so every caller keys cache entries
 * consistently (provider/model + prompt).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptCacheService {

    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /**
     * Returns the cached value for the key deserialized into the given type,
     * or {@code null} if there is no cache entry (or it cannot be deserialized).
     */
    public <T> T get(String cacheKey, Class<T> type) {
        String cached = redis.opsForValue().get(redisKey(cacheKey));
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached, type);
        } catch (Exception e) {
            log.warn("Failed to deserialize cached response; ignoring cache entry", e);
            return null;
        }
    }

    /**
     * Serializes the value to JSON and caches it against the key with a 10-minute TTL.
     */
    public void put(String cacheKey, Object value) {
        try {
            String serialized = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(redisKey(cacheKey), serialized, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to serialize value for caching; skipping cache write", e);
        }
    }

    // ---------------------------------------------------------------------
    // Streaming support
    // ---------------------------------------------------------------------

    /**
     * Returns a {@link Flux} that replays a cached stream as SSE-style chunks
     * (a content chunk followed by a terminal {@code completed} chunk), or
     * {@code null} if there is no cached entry for the key.
     */
    public Flux<StreamingChunk> getStream(String cacheKey) {
        StreamCacheEntry entry = get(cacheKey, StreamCacheEntry.class);
        if (entry == null) {
            return null;
        }
        log.info(
                "Prompt cache HIT (stream) - replaying cached stream. provider={} model={}",
                entry.getProvider(),
                entry.getModel()
        );
        StreamingChunk content = StreamingChunk.builder()
                .provider(entry.getProvider())
                .model(entry.getModel())
                .content(entry.getContent())
                .completed(false)
                .error(false)
                .build();
        StreamingChunk done = StreamingChunk.builder()
                .provider(entry.getProvider())
                .model(entry.getModel())
                .content("")
                .completed(true)
                .error(false)
                .build();
        return Flux.just(content, done);
    }

    /**
     * Wraps a live provider stream so its content is accumulated and cached once
     * the stream completes successfully. If any chunk carries an error, nothing
     * is cached. The returned {@link Flux} is otherwise identical to {@code source}.
     */
    public Flux<StreamingChunk> cacheStream(
            String cacheKey,
            String provider,
            String model,
            Flux<StreamingChunk> source
    ) {
        StringBuilder accumulated = new StringBuilder();
        AtomicBoolean failed = new AtomicBoolean(false);

        return source
                .doOnNext(chunk -> {
                    if (chunk.isError()) {
                        failed.set(true);
                    } else if (chunk.getContent() != null) {
                        accumulated.append(chunk.getContent());
                    }
                })
                .doOnComplete(() -> {
                    if (failed.get()) {
                        return;
                    }
                    put(cacheKey, new StreamCacheEntry(provider, model, accumulated.toString()));
                    log.info(
                            "Prompt cache STORE (stream) - cached completed stream. provider={} model={}",
                            provider,
                            model
                    );
                });
    }

    // ---------------------------------------------------------------------
    // Key builders
    // ---------------------------------------------------------------------

    /** Cache key for a non-streaming completion (model + prompt). */
    public String completionKey(String model, String prompt) {
        return "completion|" + model + "|" + prompt;
    }

    /** Cache key for a streamed completion (provider + model + prompt). */
    public String streamKey(String provider, String model, String prompt) {
        return "stream|" + provider + "|" + model + "|" + prompt;
    }

    private String redisKey(String cacheKey) {
        // Normalize (lowercase) so equivalent prompts hit the same cache entry
        String normalized = cacheKey == null ? "" : cacheKey.toLowerCase(Locale.ROOT);
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
