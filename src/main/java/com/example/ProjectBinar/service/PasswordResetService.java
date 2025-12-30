package com.example.ProjectBinar.service;

import com.example.ProjectBinar.entity.PasswordResetToken;
import com.example.ProjectBinar.entity.User;
import com.example.ProjectBinar.repository.PasswordResetTokenRepository;
import com.example.ProjectBinar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Password Reset Service - Menangani logic reset password.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${password-reset.token-expiry-minutes:30}")
    private int tokenExpiryMinutes;

    /**
     * Proses permintaan forgot password.
     * Generate token dan kirim email.
     * 
     * @param email Email user
     * @return true jika email berhasil dikirim
     */
    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Forgot password request for non-existent email: {}", email);
            // Return true untuk security (tidak bocorkan info email ada atau tidak)
            return true;
        }

        User user = userOpt.get();

        // Hapus token lama jika ada
        tokenRepository.findByUser(user).ifPresent(token -> {
            tokenRepository.delete(token);
        });

        // Generate token baru
        String token = generateToken();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getUsername());

        // Kirim email
        emailService.sendPasswordResetEmail(email, token);

        return true;
    }

    /**
     * Reset password dengan token.
     * 
     * @param token       Reset token
     * @param newPassword Password baru
     * @return true jika password berhasil direset
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("Invalid reset token: {}", token);
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            log.warn("Token expired or already used: {}", token);
            return false;
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());
        return true;
    }

    /**
     * Validate token tanpa mereset password.
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().isValid();
    }

    /**
     * Generate unique token.
     */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
