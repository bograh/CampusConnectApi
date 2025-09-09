package com.campusconnect.api.controller;

import com.campusconnect.api.dto.delivery.CompleteDeliveryRequestDTO;
import com.campusconnect.api.dto.delivery.CreateDeliveryRequestDTO;
import com.campusconnect.api.dto.delivery.DeliveryRequestResponseDTO;
import com.campusconnect.api.service.DeliveryRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-requests")
@RequiredArgsConstructor
public class DeliveryRequestController {

    private final DeliveryRequestService deliveryRequestService;

    @PostMapping
    public ResponseEntity<DeliveryRequestResponseDTO> createDeliveryRequest(
            @Valid @RequestBody CreateDeliveryRequestDTO request,
            HttpServletRequest httpRequest) {
        DeliveryRequestResponseDTO response = deliveryRequestService.createDeliveryRequest(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryRequestResponseDTO>> getAvailableDeliveryRequests() {
        List<DeliveryRequestResponseDTO> requests = deliveryRequestService.getAvailableDeliveryRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<DeliveryRequestResponseDTO> getDeliveryRequestById(@PathVariable String requestId) {
        DeliveryRequestResponseDTO request = deliveryRequestService.getDeliveryRequestById(requestId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<DeliveryRequestResponseDTO>> getUserDeliveryRequests(
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        List<DeliveryRequestResponseDTO> requests = deliveryRequestService.getUserDeliveryRequests(userId, status);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<DeliveryRequestResponseDTO> acceptDeliveryRequest(
            @PathVariable String requestId,
            @RequestParam String tripId,
            HttpServletRequest httpRequest) {
        DeliveryRequestResponseDTO response = deliveryRequestService.acceptDeliveryRequest(requestId, tripId, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/in-transit")
    public ResponseEntity<DeliveryRequestResponseDTO> markInTransit(
            @PathVariable String requestId,
            HttpServletRequest httpRequest) {
        DeliveryRequestResponseDTO response = deliveryRequestService.markInTransit(requestId, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/complete")
    public ResponseEntity<DeliveryRequestResponseDTO> completeDelivery(
            @PathVariable String requestId,
            @Valid @RequestBody CompleteDeliveryRequestDTO completeRequest,
            HttpServletRequest httpRequest) {
        DeliveryRequestResponseDTO response = deliveryRequestService.completeDelivery(requestId, completeRequest, httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<String> cancelDeliveryRequest(
            @PathVariable String requestId,
            HttpServletRequest httpRequest) {
        deliveryRequestService.cancelDeliveryRequest(requestId, httpRequest);
        return ResponseEntity.ok("Delivery request cancelled successfully");
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        return "user-id";
    }
}
