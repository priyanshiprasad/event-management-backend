package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.dto.request.LoginRequest;
import com.eventmgmt.event_management.dto.request.RegisterRequest;
import com.eventmgmt.event_management.dto.response.AuthResponse;
import com.eventmgmt.event_management.entity.User;
import com.eventmgmt.event_management.enums.Role;
import com.eventmgmt.event_management.exception.ResourceNotFoundException;
import com.eventmgmt.event_management.repository.UserRepository;
import com.eventmgmt.event_management.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final VerificationEmailService verificationEmailService;

    public String register(RegisterRequest request) {

        if (request.getRole() != null &&
                request.getRole().toUpperCase().equals("ADMIN")) {
            throw new RuntimeException("Cannot register as Admin.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = Role.ATTENDEE;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + request.getRole());
            }
        }

        // Generate unique verification token
        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .verified(false)
                .verificationToken(verificationToken)
                .build();

        userRepository.save(user);

        // Send verification email
        verificationEmailService.sendVerificationEmail(user);

        return "Registration successful. Please check your email to verify your account.";
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if email is verified
        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in. Check your inbox.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification link."));

        if (user.isVerified()) {
            return "Email already verified. You can login now.";
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Email verified successfully! You can now login.";
    }

    public String resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        verificationEmailService.sendVerificationEmail(user);
        return "Verification email resent. Please check your inbox.";
    }
}