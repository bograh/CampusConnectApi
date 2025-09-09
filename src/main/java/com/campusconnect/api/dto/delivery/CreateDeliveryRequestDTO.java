package com.campusconnect.api.dto.delivery;

import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.enums.ItemSize;
import com.campusconnect.api.entity.enums.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDeliveryRequestDTO {
    @NotNull(message = "Pickup location is required")
    @Valid
    private LocationDTO pickupLocation;

    @NotNull(message = "Dropoff location is required")
    @Valid
    private LocationDTO dropoffLocation;

    @NotBlank(message = "Item description is required")
    @Size(max = 500, message = "Item description cannot exceed 500 characters")
    private String itemDescription;

    @NotNull(message = "Item size is required")
    private ItemSize itemSize;

    private Priority priority = Priority.NORMAL;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal paymentAmount;

    @NotBlank(message = "Pickup date is required")
    private String pickupDate;

    @NotBlank(message = "Pickup time is required")
    private String pickupTime;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    private String specialInstructions;
}
