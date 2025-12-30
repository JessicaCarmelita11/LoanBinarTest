package com.example.ProjectBinar.controller;

import com.example.ProjectBinar.dto.AuthRequest;
import com.example.ProjectBinar.dto.AuthResponse;
import com.example.ProjectBinar.dto.ForgotPasswordRequest;
import com.example.ProjectBinar.dto.ResetPasswordRequest;
import com.example.ProjectBinar.service.AuthService;
import com.example.ProjectBinar.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth Controller - Endpoint untuk autentikasi.
 * 
 * Endpoints:
 * - POST /auth/login - Login dengan username/password
 * - POST /auth/forgot-password - Request reset password
 * - POST /auth/reset-password - Reset password dengan token
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * POST /auth/login - Login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());
            AuthResponse response = authService.authenticate(request);
            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication failed",
                    "message", "Invalid username or password"));
        }
    }

    /**
     * POST /auth/forgot-password - Request reset password.
     * 
     * Request body:
     * {
     * "email": "user@example.com"
     * }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        boolean success = passwordResetService.processForgotPassword(request.getEmail());

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If the email exists, a password reset link has been sent."));
    }

    /**
     * POST /auth/reset-password - Reset password dengan token.
     * 
     * Request body:
     * {
     * "token": "ABC12345",
     * "newPassword": "newpassword123"
     * }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password attempt with token: {}", request.getToken());

        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password has been reset successfully."));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid or expired token."));
        }
    }

    /**
     * GET /auth/validate-token - Validate reset token.
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);

        return ResponseEntity.ok(Map.of(
                "valid", valid));
    }

    /**
     * GET /auth/test - Test endpoint (public).
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint is working!");
    }
}
