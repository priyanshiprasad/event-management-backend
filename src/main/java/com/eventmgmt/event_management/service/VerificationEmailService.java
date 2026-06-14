package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Change this to your backend URL when deployed
    private static final String BASE_URL = "http://localhost:8080";

    @Async
    public void sendVerificationEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("EventMgr <" + fromEmail + ">");
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your EventMgr Account");
            helper.setText(buildVerificationHtml(user), true);

            mailSender.send(message);
            System.out.println("Verification email sent to: " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    private String buildVerificationHtml(User user) {
        String verificationLink = BASE_URL + "/api/auth/verify?token=" + user.getVerificationToken();

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#0a0608;font-family:'Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0608;padding:40px 20px;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0" style="background:#13090c;border:1px solid rgba(232,201,126,0.2);">

                    <!-- Gold bar -->
                    <tr><td style="background:#e8c97e;padding:10px;text-align:center;">
                      <span style="font-size:11px;font-weight:700;color:#1a0e05;letter-spacing:3px;">VERIFY YOUR ACCOUNT</span>
                    </td></tr>

                    <!-- Header -->
                    <tr><td style="padding:36px 40px 24px;text-align:center;border-bottom:1px solid rgba(232,201,126,0.1);">
                      <p style="margin:0 0 6px;font-size:28px;font-weight:800;color:#e8c97e;">EventMgr</p>
                      <p style="margin:0;font-size:13px;color:rgba(255,255,255,0.5);">One step away from your account</p>
                    </td></tr>

                    <!-- Body -->
                    <tr><td style="padding:36px 40px;">
                      <p style="margin:0 0 8px;font-size:13px;color:rgba(255,255,255,0.5);">Hello,</p>
                      <p style="margin:0 0 24px;font-size:20px;font-weight:700;color:#fff;">%s</p>

                      <p style="margin:0 0 28px;font-size:14px;color:rgba(255,255,255,0.55);line-height:1.7;font-weight:300;">
                        Thank you for registering on EventMgr. Please verify your email address by clicking the button below to activate your account.
                      </p>

                      <!-- Verify button -->
                      <table cellpadding="0" cellspacing="0" style="margin:0 auto 28px;">
                        <tr><td style="background:#e8c97e;clip-path:polygon(0 0,calc(100%% - 10px) 0,100%% 10px,100%% 100%%,10px 100%%,0 calc(100%% - 10px));">
                          <a href="%s" style="display:block;padding:16px 40px;font-size:14px;font-weight:700;color:#1a0e05;text-decoration:none;letter-spacing:1px;text-transform:uppercase;">
                            Verify My Email →
                          </a>
                        </td></tr>
                      </table>

                      <!-- Link fallback -->
                      <div style="background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.06);padding:14px 16px;margin-bottom:24px;">
                        <p style="margin:0 0 6px;font-size:10px;color:rgba(255,255,255,0.3);letter-spacing:1px;text-transform:uppercase;">Or copy this link</p>
                        <p style="margin:0;font-size:11px;color:#e8c97e;word-break:break-all;">%s</p>
                      </div>

                      <p style="margin:0;font-size:12px;color:rgba(255,255,255,0.25);line-height:1.6;">
                        This link will remain active. If you did not create an account on EventMgr, you can safely ignore this email.
                      </p>
                    </td></tr>

                    <!-- Footer -->
                    <tr><td style="padding:20px 40px 28px;border-top:1px solid rgba(232,201,126,0.1);text-align:center;">
                      <p style="margin:0;font-size:11px;color:rgba(255,255,255,0.25);">EventMgr · Your Event Platform</p>
                    </td></tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(user.getName(), verificationLink, verificationLink);
    }
}
