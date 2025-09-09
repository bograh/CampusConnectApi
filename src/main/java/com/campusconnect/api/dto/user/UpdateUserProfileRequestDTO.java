package com.campusconnect.api.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequestDTO {
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^(\\+233|0)[0-9]{9}$", message = "Please enter a valid Ghana phone number")
    private String phoneNumber;

    private String profileImage;
}
