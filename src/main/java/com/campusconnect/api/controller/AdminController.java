package com.campusconnect.api.controller;

import com.campusconnect.api.dto.user.UserProfileResponseDTO;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.enums.VerificationStatus;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserProfileResponseDTO> userProfiles = users.stream()
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userProfiles);
    }

    @GetMapping("/users/pending-verification")
    public ResponseEntity<List<UserProfileResponseDTO>> getPendingVerificationUsers() {
        List<User> users = userRepository.findByVerificationStatus(VerificationStatus.PENDING_VERIFICATION);
        List<UserProfileResponseDTO> userProfiles = users.stream()
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userProfiles);
    }

    @PutMapping("/users/{userId}/verification-status")
    public ResponseEntity<String> updateVerificationStatus(
            @PathVariable String userId,
            @RequestParam VerificationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setVerificationStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        
        if (status == VerificationStatus.VERIFIED) {
            user.setStudentIdValidated(true);
        }
        
        userRepository.save(user);

        return ResponseEntity.ok("User verification status updated to " + status);
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setIsOnline(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("User activated successfully");
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setIsOnline(false);
        user.setLastSeen(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("User deactivated successfully");
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        long totalUsers = userRepository.count();
        long verifiedUsers = userRepository.findByVerificationStatus(VerificationStatus.VERIFIED).size();
        long pendingUsers = userRepository.findByVerificationStatus(VerificationStatus.PENDING_VERIFICATION).size();

        Map<String, Long> response = new HashMap<>();
        response.put("totalUsers", totalUsers);
        response.put("verifiedUsers", verifiedUsers);
        response.put("pendingUsers", pendingUsers);

        return ResponseEntity.ok(response);
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
