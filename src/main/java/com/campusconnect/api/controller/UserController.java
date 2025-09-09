package com.campusconnect.api.controller;

import com.campusconnect.api.security.JwtService;
import com.campusconnect.api.dto.user.ChangePasswordRequestDTO;
import com.campusconnect.api.dto.user.UpdateUserProfileRequestDTO;
import com.campusconnect.api.dto.user.UserProfileResponseDTO;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.exception.UnauthorizedException;
import com.campusconnect.api.repository.UserRepository;
import com.campusconnect.api.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        UserProfileResponseDTO profile = mapToUserProfileResponse(user);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequestDTO request,
            HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        UserProfileResponseDTO profile = mapToUserProfileResponse(user);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/online-status")
    public ResponseEntity<String> updateOnlineStatus(
            @RequestParam Boolean isOnline,
            HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setIsOnline(isOnline);
        if (!isOnline) {
            user.setLastSeen(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Online status updated");
    }

    private UserProfileResponseDTO mapToUserProfileResponse(User user) {
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        profile.setId(user.getId());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setStudentId(user.getStudentId());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setPhoneVerified(user.getPhoneVerified());
        profile.setStudentIdValidated(user.getStudentIdValidated());
        profile.setVerificationStatus(user.getVerificationStatus());
        profile.setProfileImage(user.getProfileImage());
        profile.setRating(user.getRating());
        profile.setTotalDeliveries(user.getTotalDeliveries());
        profile.setJoinedDate(user.getJoinedDate());
        profile.setIsOnline(user.getIsOnline());
        profile.setLastSeen(user.getLastSeen());
        return profile;
    }
}
