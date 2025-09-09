package com.campusconnect.api.dto.review;

import com.campusconnect.api.entity.enums.ReviewType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDTO {
    private String id;
    private String reviewerId;
    private String reviewerName;
    private String revieweeId;
    private String revieweeName;
    private Integer rating;
    private String comment;
    private String deliveryId;
    private ReviewType type;
    private LocalDateTime createdAt;
}
