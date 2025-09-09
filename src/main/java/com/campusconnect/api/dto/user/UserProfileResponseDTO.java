package com.campusconnect.api.dto.user;

import com.campusconnect.api.entity.enums.VerificationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String studentId;
    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean studentIdValidated;
    private VerificationStatus verificationStatus;
    private String profileImage;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private LocalDateTime joinedDate;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
}

