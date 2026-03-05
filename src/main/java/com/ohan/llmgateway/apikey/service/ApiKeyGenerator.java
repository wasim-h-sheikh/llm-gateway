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

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ApiKeyGenerator {

    public String generateKey() {

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        String random = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        return "sk_live_" + random;
    }
}