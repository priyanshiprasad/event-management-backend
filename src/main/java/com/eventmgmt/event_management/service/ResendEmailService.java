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
public class ResendEmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String FROM_ADDRESS = "EventMgr <onboarding@resend.dev>";

    @Async
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + resendApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("from", FROM_ADDRESS);
            body.put("to", List.of(toEmail));
            body.put("subject", subject);
            body.put("html", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(RESEND_API_URL, request, String.class);
            System.out.println("Email sent via Resend to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email via Resend: " + e.getMessage());
        }
    }

    @Async
    public void sendEmailWithAttachment(String toEmail, String subject, String htmlContent,
                                        String fileName, byte[] attachmentBytes) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + resendApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("from", FROM_ADDRESS);
            body.put("to", List.of(toEmail));
            body.put("subject", subject);
            body.put("html", htmlContent);

            Map<String, String> attachment = new HashMap<>();
            attachment.put("filename", fileName);
            attachment.put("content", java.util.Base64.getEncoder().encodeToString(attachmentBytes));
            body.put("attachments", List.of(attachment));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(RESEND_API_URL, request, String.class);
            System.out.println("Email with attachment sent via Resend to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email with attachment via Resend: " + e.getMessage());
        }
    }
}
