package com.eventmgmt.event_management.controller;

import com.eventmgmt.event_management.dto.request.LoginRequest;
import com.eventmgmt.event_management.dto.request.RegisterRequest;
import com.eventmgmt.event_management.dto.response.AuthResponse;
import com.eventmgmt.event_management.service.AuthService;
import com.eventmgmt.event_management.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        String result = authService.verifyEmail(token);
        // Redirect to login page after verification
        return ResponseEntity.status(302)
                .header("Location", "http://localhost:5173/login?verified=true")
                .build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> body) {
        String message = authService.resendVerification(body.get("email"));
        return ResponseEntity.ok(Map.of("message", message));
    }

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {
        String message = passwordResetService.requestPasswordReset(body.get("email"));
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> body) {
        String message = passwordResetService.resetPassword(
                body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", message));
    }
}