package com.campusconnect.api.dto.review;

import com.campusconnect.api.entity.enums.ReviewType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequestDTO {
    @NotBlank(message = "Reviewee ID is required")
    private String revieweeId;

    @NotBlank(message = "Delivery ID is required")
    private String deliveryId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @NotNull(message = "Review type is required")
    private ReviewType type;
}
