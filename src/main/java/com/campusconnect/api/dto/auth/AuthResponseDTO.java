package com.campusconnect.api.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuthResponseDTO {
    private String message;
    private String token;
    private UserData user;

    @Getter
    @Setter
    public static class UserData {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String studentId;
        private String phonNumber;
        private boolean phoneVerified;
        private boolean studentIdValidated;
        private int studentIdValidationScore;
        private String verificationStatus;
        private StudentIDImg studentIdImage;
        private SelfieImg selfieImage;
        private double rating;
        private long totalDeliveries;
        private LocalDateTime joinedDate;
    }

    @Getter
    @Setter
    public static class StudentIDImg {
        private String url;
        private String publicId;
    }

    @Getter
    @Setter
    public static class SelfieImg {
        private String url;
        private String publicId;
    }
}
