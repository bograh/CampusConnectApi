package com.campusconnect.api.dto.delivery;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteDeliveryRequestDTO {
    private String deliveryProof;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
