/*
 *
 *  * Copyright (c) 2026 Wasim Sheikh
 *  * Project: LLM Gateway
 *  *
 *  * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  * Proprietary and confidential.
 *
 */

package com.ohan.llmgateway.auth;

import com.ohan.llmgateway.auth.dto.AuthResponse;
import com.ohan.llmgateway.auth.dto.LoginRequest;
import com.ohan.llmgateway.auth.dto.RegisterRequest;
import com.ohan.llmgateway.auth.entity.RefreshToken;
import com.ohan.llmgateway.auth.jwt.JwtService;
import com.ohan.llmgateway.auth.service.AuthService;
import com.ohan.llmgateway.auth.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        AuthResponse authResponse = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // true in production (HTTPS)
                .path("/api/auth/refresh")
                .maxAge(60L * 60 * 24 * 30)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                Map.of("accessToken", authResponse.getAccessToken())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue("refreshToken") String oldToken,
            HttpServletResponse response
    ) {

        RefreshToken refreshToken = refreshTokenService.validate(oldToken);

        // ROTATION: mark old token revoked
        refreshToken.setRevoked(true);
        refreshTokenService.save(refreshToken);

        // create new refresh token
        RefreshToken newRefresh =
                refreshTokenService.createRefreshToken(refreshToken.getUser());

        String newAccessToken =
                jwtService.generateToken(refreshToken.getUser());

        // set new cookie
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", newRefresh.getToken())
                .httpOnly(true)
                .secure(false) // true in production
                .path("/api/auth/refresh")
                .maxAge(60L * 60 * 24 * 30)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken != null) {
            refreshTokenService.logout(refreshToken);
        }

        // delete cookie
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true in production
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok("Logged out successfully");
    }
}
