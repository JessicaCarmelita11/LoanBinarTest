package com.example.ProjectBinar.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Email Service - Menangani pengiriman email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    /**
     * Kirim email reset password.
     * 
     * @param to         Email tujuan
     * @param resetToken Token reset password
     */
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset Request - Project Binar");

            String htmlContent = buildPasswordResetEmailContent(resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Build HTML content untuk email reset password.
     */
    private String buildPasswordResetEmailContent(String resetToken) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4A90D9; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .token-box { background-color: #fff; border: 2px solid #4A90D9; padding: 15px;
                                     text-align: center; font-size: 24px; letter-spacing: 3px;
                                     font-weight: bold; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Password Reset</h1>
                        </div>
                        <div class="content">
                            <p>Halo,</p>
                            <p>Kami menerima permintaan untuk mereset password akun Anda.</p>
                            <p>Gunakan token berikut untuk mereset password Anda:</p>
                            <div class="token-box">%s</div>
                            <p><strong>Token ini akan expired dalam 30 menit.</strong></p>
                            <p>Jika Anda tidak meminta reset password, abaikan email ini.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 Project Binar. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetToken);
    }
}
