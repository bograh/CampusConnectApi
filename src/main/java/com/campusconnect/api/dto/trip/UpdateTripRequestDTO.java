package com.campusconnect.api.dto.trip;

import com.campusconnect.api.entity.enums.TripStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTripRequestDTO {
    private String departureTime;
    private BigDecimal pricePerDelivery;
    private Integer maxDeliveries;
    private TripStatus status;
    private String description;
}
