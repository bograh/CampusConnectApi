package com.campusconnect.api.dto.delivery;

import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.enums.ItemSize;
import com.campusconnect.api.entity.enums.Priority;
import com.campusconnect.api.entity.enums.RequestStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class DeliveryRequestResponseDTO {
    private String id;
    private String userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private BigDecimal userRating;
    private LocationDTO pickupLocation;
    private LocationDTO dropoffLocation;
    private String itemDescription;
    private ItemSize itemSize;
    private Priority priority;
    private BigDecimal paymentAmount;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private String contactInfo;
    private String specialInstructions;
    private RequestStatus status;
    private String matchedTripId;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
