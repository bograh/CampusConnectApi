package com.campusconnect.api.dto.trip;

import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.enums.TransportMethod;
import com.campusconnect.api.entity.enums.TripStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TripResponseDTO {
    private String id;
    private String travelerId;
    private String travelerName;
    private String travelerPhone;
    private BigDecimal travelerRating;
    private LocationDTO fromLocation;
    private LocationDTO toLocation;
    private LocalDateTime departureTime;
    private TransportMethod transportMethod;
    private Integer maxDeliveries;
    private Integer currentDeliveries;
    private BigDecimal pricePerDelivery;
    private Boolean isRecurring;
    private TripStatus status;
    private String description;
    private LocalDateTime createdAt;
}
