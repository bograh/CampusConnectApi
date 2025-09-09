package com.campusconnect.api.controller;

import com.campusconnect.api.dto.auth.AuthResponseDTO;
import com.campusconnect.api.dto.auth.PhoneVerificationRequestDTO;
import com.campusconnect.api.dto.auth.SignInRequestDTO;
import com.campusconnect.api.dto.auth.SignupRequestDTO;
import com.campusconnect.api.dto.user.UserProfileResponseDTO;
import com.campusconnect.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        AuthResponseDTO response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponseDTO> signin(@Valid @RequestBody SignInRequestDTO signInRequestDTO) {
        AuthResponseDTO response = userService.signin(signInRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser(HttpServletRequest request) {
        UserProfileResponseDTO user = userService.getCurrentUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(
            HttpServletRequest request,
            @Valid @RequestBody PhoneVerificationRequestDTO phoneVerificationRequestDTO) {
        userService.verifyPhone(request, phoneVerificationRequestDTO);
        return ResponseEntity.ok("Phone number verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationCode(HttpServletRequest request) {
        userService.resendVerificationCode(request);
        return ResponseEntity.ok("Verification code sent successfully");
    }
}
