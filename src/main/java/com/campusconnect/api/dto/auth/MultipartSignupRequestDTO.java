package com.campusconnect.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartSignupRequestDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(regexp = ".*@st\\.knust\\.edu\\.gh$", message = "Please use your university email address (@st.knust.edu.gh)")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+233|0)[0-9]{9}$", message = "Please enter a valid Ghana phone number")
    private String phoneNumber;

    private MultipartFile studentIdImage;
    private MultipartFile selfieImage;
}
