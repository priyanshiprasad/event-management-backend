package com.eventmgmt.event_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Async
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            Map<String, Object> body = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", "EventMgr");
            sender.put("email", fromEmail);
            body.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            body.put("to", List.of(recipient));

            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(BREVO_API_URL, request, String.class);
            System.out.println("Email sent via Brevo to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email via Brevo: " + e.getMessage());
        }
    }

    @Async
    public void sendEmailWithAttachment(String toEmail, String subject, String htmlContent,
                                        String fileName, byte[] attachmentBytes) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            Map<String, Object> body = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", "EventMgr");
            sender.put("email", fromEmail);
            body.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            body.put("to", List.of(recipient));

            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            Map<String, String> attachment = new HashMap<>();
            attachment.put("name", fileName);
            attachment.put("content", java.util.Base64.getEncoder().encodeToString(attachmentBytes));
            body.put("attachment", List.of(attachment));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(BREVO_API_URL, request, String.class);
            System.out.println("Email with attachment sent via Brevo to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email with attachment via Brevo: " + e.getMessage());
        }
    }
}
