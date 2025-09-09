package com.campusconnect.api.dto.trip;

import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.enums.TransportMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateTripRequestDTO  {
    @NotNull(message = "From location is required")
    @Valid
    private LocationDTO fromLocation;

    @NotNull(message = "To location is required")
    @Valid
    private LocationDTO toLocation;

    @NotBlank(message = "Departure date is required")
    private String departureDate;

    @NotBlank(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Available seats is required")
    @Min(value = 1, message = "Available seats must be at least 1")
    @Max(value = 10, message = "Available seats cannot exceed 10")
    private Integer availableSeats;

    @NotNull(message = "Price per delivery is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerDelivery;

    @NotNull(message = "Vehicle type is required")
    private TransportMethod vehicleType;

    private Boolean recurring = false;

    private String description;
    private String contactInfo;
}
