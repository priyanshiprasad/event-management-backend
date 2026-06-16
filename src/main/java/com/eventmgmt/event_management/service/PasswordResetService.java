package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.entity.User;
import com.eventmgmt.event_management.exception.ResourceNotFoundException;
import com.eventmgmt.event_management.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email."));

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email first before resetting password.");
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        sendResetEmail(user, token);
        return "Password reset link sent to your email.";
    }

    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset link has expired. Please request a new one.");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successfully. You can now login.";
    }

    @Async
    public void sendResetEmail(User user, String token) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("EventMgr <" + fromEmail + ">");
            helper.setTo(user.getEmail());
            helper.setSubject("Reset Your EventMgr Password");
            helper.setText(buildResetEmailHtml(user.getName(), resetLink), true);

            mailSender.send(message);
            System.out.println("Password reset email sent to: " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
        }
    }

    private String buildResetEmailHtml(String name, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#0a0608;font-family:'Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0608;padding:40px 20px;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0" style="background:#13090c;border:1px solid rgba(232,201,126,0.2);">

                    <tr><td style="background:#e8c97e;padding:10px;text-align:center;">
                      <span style="font-size:11px;font-weight:700;color:#1a0e05;letter-spacing:3px;">PASSWORD RESET</span>
                    </td></tr>

                    <tr><td style="padding:36px 40px 24px;text-align:center;border-bottom:1px solid rgba(232,201,126,0.1);">
                      <p style="margin:0 0 6px;font-size:28px;font-weight:800;color:#e8c97e;">EventMgr</p>
                      <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.5);">Reset your password</p>
                    </td></tr>

                    <tr><td style="padding:36px 40px;">
                      <p style="margin:0 0 8px;font-size:13px;color:rgba(255,255,255,0.5);">Hello,</p>
                      <p style="margin:0 0 24px;font-size:20px;font-weight:700;color:#fff;">%s</p>

                      <p style="margin:0 0 28px;font-size:14px;color:rgba(255,255,255,0.55);line-height:1.7;font-weight:300;">
                        We received a request to reset your EventMgr password. Click the button below to set a new password. This link expires in <strong style="color:#e8c97e;">1 hour</strong>.
                      </p>

                      <table cellpadding="0" cellspacing="0" style="margin:0 auto 28px;">
                        <tr><td style="background:#e8c97e;">
                          <a href="%s" style="display:block;padding:16px 40px;font-size:14px;font-weight:700;color:#1a0e05;text-decoration:none;letter-spacing:1px;text-transform:uppercase;">
                            Reset My Password →
                          </a>
                        </td></tr>
                      </table>

                      <div style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.06);padding:14px 16px;margin-bottom:24px;">
                        <p style="margin:0 0 6px;font-size:10px;color:rgba(255,255,255,0.3);letter-spacing:1px;text-transform:uppercase;">Or copy this link</p>
                        <p style="margin:0;font-size:11px;color:#e8c97e;word-break:break-all;">%s</p>
                      </div>

                      <p style="margin:0;font-size:12px;color:rgba(255,255,255,0.25);line-height:1.6;">
                        If you did not request a password reset please ignore this email. Your password will not be changed.
                      </p>
                    </td></tr>

                    <tr><td style="padding:20px 40px 28px;border-top:1px solid rgba(232,201,126,0.1);text-align:center;">
                      <p style="margin:0;font-size:11px;color:rgba(255,255,255,0.25);">EventMgr · Your Event Platform</p>
                    </td></tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(name, resetLink, resetLink);
    }
}