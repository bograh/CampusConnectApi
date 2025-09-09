package com.campusconnect.api.controller;

import com.campusconnect.api.dto.PaginatedResponseDTO;
import com.campusconnect.api.dto.trip.CreateTripRequestDTO;
import com.campusconnect.api.dto.trip.TripResponseDTO;
import com.campusconnect.api.dto.trip.UpdateTripRequestDTO;
import com.campusconnect.api.service.TripService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(
            HttpServletRequest request,
            @Valid @RequestBody CreateTripRequestDTO createTripRequestDTO) {
        TripResponseDTO trip = tripService.createTrip(createTripRequestDTO, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<TripResponseDTO>> getAllTrips(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String departureDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "35") int limit) {

        PaginatedResponseDTO<TripResponseDTO> response = tripService.getAllTrips(from, to, departureDate, page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable String tripId) {
        TripResponseDTO trip = tripService.getTripById(tripId);
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable String tripId,
            @Valid @RequestBody UpdateTripRequestDTO updateTripRequestDTO,
            HttpServletRequest request) {
        TripResponseDTO trip = tripService.updateTrip(tripId, updateTripRequestDTO, request);
        return ResponseEntity.ok(trip);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<String> cancelTrip(
            HttpServletRequest request,
            @PathVariable String tripId) {
        tripService.cancelTrip(tripId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<TripResponseDTO>> getUserTrips(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        List<TripResponseDTO> trips = tripService.getUserTrips(status, request);
        return ResponseEntity.ok(trips);
    }
}
