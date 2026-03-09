/*
 * Copyright (c) 2026 Wasim Sheikh
 * Project: LLM Gateway
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package com.ohan.llmgateway;

import com.ohan.llmgateway.provider.ProvidersConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LlmGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LlmGatewayApplication.class, args);
	}

}
