/*
 * Copyright (c) 2026 Wasim Sheikh
 * Project: LLM Gateway
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package com.ohan.llmgateway.auth.repository;

import com.ohan.llmgateway.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}